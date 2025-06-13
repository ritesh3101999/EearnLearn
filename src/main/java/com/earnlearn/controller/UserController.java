package com.earnlearn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.earnlearn.model.User;
import com.earnlearn.service.RoleService;
import com.earnlearn.service.UserService;
import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {
	@Autowired
	private UserService userService;
	@Autowired
	private RoleService roleService;

	// Show registration form
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		// Fetch all roles for selection
		model.addAttribute("roles", roleService.findAll());
		return "dashboard";
	}

	// Handle user registration with role assignment.
	@PostMapping("/register")
	public String registerUser(User user, @RequestParam String roleName, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			// Save the user along with the selected role
			userService.save(user, roleName);
			// RedirectAttributes is used for flash message
			redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
			return "redirect:/dashboard";
		} catch (IllegalArgumentException e) {
			model.addAttribute("errorMessage", e.getMessage());
			model.addAttribute("user", user);
			model.addAttribute("roles", roleService.findAll()); // Re-populate roles in case of error
			return "dashboard";
		}
	}

	// Display login form
	@GetMapping("/login")
	public String showLoginForm() {
		return "redirect:/dashboard";
	}

	// Handle logout redirection
	@GetMapping("/logout")
	public String logout() {
		return "redirect:/users/login?logout";
	}

	@GetMapping("/list")
	public String listAllUsers(Model model) {
		List<User> users = userService.findAllUsers();
		model.addAttribute("users", users);
		return "user-list";
	}

	// Directly delete by ID
	@GetMapping("/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		userService.deleteUserById(id);
		redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
		return "redirect:/users/list";
	}
}
