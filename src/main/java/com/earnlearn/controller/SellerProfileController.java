package com.earnlearn.controller;

import com.earnlearn.model.SellerProfile;
import com.earnlearn.model.User;
import com.earnlearn.service.SellerProfileService;
import com.earnlearn.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/seller-profiles")
public class SellerProfileController {

	@Autowired
	private SellerProfileService sellerProfileService;

	@Autowired
	private UserRepository userRepository; // To get the User object from username/ID

	@GetMapping("/{userId}")
	public String viewSellerProfile(@PathVariable Long userId, Model model, RedirectAttributes redirectAttributes) {
		Optional<SellerProfile> sellerProfileOptional = sellerProfileService.getSellerProfileByUserId(userId);

		if (sellerProfileOptional.isPresent()) {
			model.addAttribute("sellerProfile", sellerProfileOptional.get());
			return "seller_profiles/view";
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Seller profile not found.");
			return "redirect:/"; // Redirect to homepage or a generic error page
		}
	}

	@GetMapping("/my-profile/edit")
	public String showEditSellerProfileForm(Model model, Principal principal, RedirectAttributes redirectAttributes) {
		if (principal == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to edit your profile.");
			return "redirect:/login"; // Redirect to login if not authenticated
		}

		String username = principal.getName();
		User currentUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Logged-in user not found in database."));

		// Check if the user has the 'SELLER' role
		boolean isSeller = currentUser.getRoles().stream().anyMatch(role -> "SELLER".equals(role.getName()));

		if (!isSeller) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"You do not have permission to access a seller profile.");
			return "redirect:/"; // Or redirect to a page explaining they need to be a seller
		}

		Optional<SellerProfile> sellerProfileOptional = sellerProfileService
				.getSellerProfileByUserId(currentUser.getId());
		SellerProfile sellerProfile;

		if (sellerProfileOptional.isPresent()) {
			sellerProfile = sellerProfileOptional.get();
		} else {
			// If no profile exists, create a new empty one for the user
			sellerProfile = new SellerProfile();
			sellerProfile.setUser(currentUser); // Link the new profile to the current user
		}

		model.addAttribute("sellerProfile", sellerProfile);
		model.addAttribute("currentUser", currentUser); // Pass current user for context
		return "seller_profiles/edit";
	}

	@PostMapping("/my-profile/save")
	public String saveSellerProfile(@ModelAttribute SellerProfile sellerProfile, Principal principal,
			RedirectAttributes redirectAttributes) {
		if (principal == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "You must be logged in to save your profile.");
			return "redirect:/login";
		}

		String username = principal.getName();
		User currentUser = userRepository.findByUsername(username)
				.orElseThrow(() -> new RuntimeException("Logged-in user not found in database."));

		// Ensure the profile being saved belongs to the current user
		// If the profile ID is present, fetch it to ensure it matches the current user
		if (sellerProfile.getId() != null) {
			Optional<SellerProfile> existingProfile = sellerProfileService.getSellerProfileById(sellerProfile.getId());
			if (existingProfile.isEmpty() || !existingProfile.get().getUser().getId().equals(currentUser.getId())) {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Unauthorized attempt to modify a seller profile.");
				return "redirect:/";
			}
		}

		try {
			sellerProfileService.saveSellerProfile(currentUser.getId(), sellerProfile);
			redirectAttributes.addFlashAttribute("successMessage", "Seller profile saved successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error saving seller profile: " + e.getMessage());
		}

		return "redirect:/seller-profiles/" + currentUser.getId(); // Redirect to their public profile
	}
}