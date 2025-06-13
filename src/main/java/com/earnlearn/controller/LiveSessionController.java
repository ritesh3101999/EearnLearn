package com.earnlearn.controller;

import com.earnlearn.model.LiveSession;
import com.earnlearn.model.User;
import com.earnlearn.service.LiveSessionService;
import com.earnlearn.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/live-sessions")
public class LiveSessionController {

	private final LiveSessionService liveSessionService;
	private final UserService userService;

	public LiveSessionController(LiveSessionService liveSessionService, UserService userService) {
		this.liveSessionService = liveSessionService;
		this.userService = userService;
	}

	private User getCurrentUser(UserDetails userDetails) {
		if (userDetails == null) {
			throw new RuntimeException(
					"User details not available. Ensure the endpoint is secured and the user is authenticated.");
		}
		return userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found for username: " + userDetails.getUsername()));
	}

	@GetMapping
	public String listLiveSessions(Model model) {
		model.addAttribute("liveSessions", liveSessionService.getAllLiveSessions());
		return "livesessions/list";
	}

	@GetMapping("/new")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
	public String showLiveSessionForm(Model model) {
		model.addAttribute("liveSession", new LiveSession());
		return "livesessions/form";
	}

	@PostMapping("/save")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
	public String saveLiveSession(@Valid @ModelAttribute("liveSession") LiveSession liveSession,
			BindingResult bindingResult, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			return "livesessions/form";
		}
		try {
			liveSessionService.saveLiveSession(liveSession, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("successMessage", "Live session saved successfully!");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "livesessions/form"; // Stay on form if validation error
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error saving live session: " + e.getMessage());
		}
		return "redirect:/live-sessions";
	}

	@GetMapping("/{id}")
	public String viewLiveSession(@PathVariable Long id, Model model) {
		LiveSession liveSession = liveSessionService.getLiveSessionById(id)
				.orElseThrow(() -> new RuntimeException("Live Session not found"));
		model.addAttribute("liveSession", liveSession);
		model.addAttribute("currentTime", LocalDateTime.now()); // For dynamic button display
		return "livesessions/detail";
	}

	@GetMapping("/edit/{id}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
	public String showEditLiveSessionForm(@PathVariable Long id, Model model,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		LiveSession liveSession = liveSessionService.getLiveSessionById(id)
				.orElseThrow(() -> new RuntimeException("Live Session not found for editing"));

		User currentUser = getCurrentUser(userDetails);
		if (!liveSession.getInstructor().getId().equals(currentUser.getId())
				&& !currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
			redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to edit this session.");
			return "redirect:/live-sessions";
		}

		model.addAttribute("liveSession", liveSession);
		return "livesessions/form";
	}

	@PostMapping("/delete/{id}")
	@PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
	public String deleteLiveSession(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		LiveSession liveSession = liveSessionService.getLiveSessionById(id)
				.orElseThrow(() -> new RuntimeException("Live Session not found for deletion"));

		User currentUser = getCurrentUser(userDetails);
		if (!liveSession.getInstructor().getId().equals(currentUser.getId())
				&& !currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ADMIN"))) {
			redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to delete this session.");
			return "redirect:/live-sessions";
		}

		try {
			liveSessionService.deleteLiveSession(id);
			redirectAttributes.addFlashAttribute("successMessage", "Live session deleted successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error deleting live session: " + e.getMessage());
		}
		return "redirect:/live-sessions";
	}
}