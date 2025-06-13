package com.earnlearn.controller;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.earnlearn.model.User;
import com.earnlearn.service.ProductService;
import com.earnlearn.service.RoleService;

@Controller
public class DashboardController {
	@Autowired
	private RoleService roleService;

	@Autowired
	private ProductService productService;

	// Provide an empty User model for the registration form in the modal
	// you have to use this ,if not loop will start
	@ModelAttribute("user")
	public User defaultUser() {
		return new User();
	}

	@GetMapping("/dashboard")
	public String showDashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		model.addAttribute("pageTitle", "EarnLearn Dashboard");
		// if you not use here roles model then roles will not show in dashboard page
//		model.addAttribute("roles", roleService.findAll());

		// Fetch all roles and filter out the "ADMIN" role before sending to the view
		model.addAttribute("roles", roleService.findAll().stream().filter(role -> !role.getName().equals("ADMIN"))
				.collect(Collectors.toList()));

		model.addAttribute("products", productService.getAllProducts());
		// Pass authentication status
		model.addAttribute("isAuthenticated", userDetails != null);
		return "dashboard";
	}
}
