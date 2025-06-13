package com.earnlearn.controller;

import com.earnlearn.model.Activity;
import com.earnlearn.model.User;
import com.earnlearn.service.ActivityService;
import com.earnlearn.service.CartService;
import com.earnlearn.service.EnrollmentService;
import com.earnlearn.service.NoteService;
import com.earnlearn.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	@Autowired
	private UserService userService;

	@Autowired
	private NoteService noteService;

	@Autowired
	private EnrollmentService enrollmentService;

	@Autowired
	private CartService cartService;

	@Autowired
	private ActivityService activityService;

	@GetMapping("/")
	public String redirectToDashboard() {
		return "dashboard";
	}

	@GetMapping("/home")
	public String showHomePage(Model model, @AuthenticationPrincipal UserDetails userDetails,
			HttpServletRequest request) {
		model.addAttribute("pageTitle", "Welcome to EarnLearn Home");
		model.addAttribute("welcomeMessage", "Explore your personalized dashboard, courses, marketplace, and notes.");

		if (userDetails != null) {
			User currentUser = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("Authenticated user not found"));

			// Get counts from services
			int notesCount = noteService.getUserNotes(currentUser).size();
			int bookmarksCount = noteService.getBookmarkedNotes(currentUser).size();
			int enrollmentsCount = enrollmentService.getUserEnrollments(currentUser).size();
			int cartCount = cartService.getCartItemCount(currentUser);

			// Add counts to the model for the view to use
			model.addAttribute("notesCount", notesCount);
			model.addAttribute("bookmarksCount", bookmarksCount);
			model.addAttribute("enrollmentsCount", enrollmentsCount);
			model.addAttribute("cartCount", cartCount);

			// Also update the session cart count for the header
			request.getSession().setAttribute("cartCount", cartCount);

			// Fetch and add recent activities to the model
			List<Activity> recentActivities = activityService.getRecentActivities(currentUser, 5); // Get last 5
																									// activities
			model.addAttribute("recentActivities", recentActivities);
			User user = userService.findByUsername(userDetails.getUsername())
					.orElseThrow(() -> new RuntimeException("User not found"));
			model.addAttribute("currentUser", user);

		} else {
			// Default values for guests
			model.addAttribute("notesCount", 0);
			model.addAttribute("bookmarksCount", 0);
			model.addAttribute("enrollmentsCount", 0);
			model.addAttribute("cartCount", 0);
			model.addAttribute("recentActivities", Collections.emptyList());
		}

		return "home";
	}
}
