package com.earnlearn.controller;

import com.earnlearn.model.User;
import com.earnlearn.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

	private final UserService userService;

	public ProfileController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public String viewProfile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
		if (userDetails == null) {
			// Should not happen if security is configured correctly, but as a safeguard
			return "redirect:/dashboard?error=notauthenticated";
		}
		User currentUser = userService.findByUsername(userDetails.getUsername()).orElseThrow(
				() -> new RuntimeException("Authenticated user not found in database: " + userDetails.getUsername()));

		model.addAttribute("user", currentUser);
		model.addAttribute("pageTitle", "My Profile");
		return "profile"; // Name of the Thymeleaf template (e.g., profile.html)
	}

	@PostMapping("/update")
	public String updateProfile(@Valid @ModelAttribute("user") User userUpdates, BindingResult bindingResult,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes, Model model) {

		if (userDetails == null) {
			return "redirect:/dashboard?error=notauthenticated";
		}

		User persistentUser = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()));

		if (!persistentUser.getId().equals(userUpdates.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error: Profile update ID mismatch.");
			return "redirect:/profile";
		}

		if (bindingResult.hasFieldErrors("name") || bindingResult.hasFieldErrors("email")
				|| bindingResult.hasFieldErrors("dateOfBirth") || bindingResult.hasFieldErrors("gender")) {

			userUpdates.setUsername(persistentUser.getUsername()); // Keep original username
			userUpdates.setRoles(persistentUser.getRoles()); // Keep original roles
			model.addAttribute("pageTitle", "My Profile");
			// bindingResult.getAllErrors().forEach(error ->
			// System.out.println(error.getDefaultMessage())); // For debugging
			redirectAttributes.addFlashAttribute("errorMessage", "Validation errors occurred. Please check the form.");
			return "redirect:/profile";
		}

		try {
			userService.updateAuthenticatedUserProfile(userDetails.getUsername(), userUpdates);
			redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
		}
		return "redirect:/profile";
	}
}
