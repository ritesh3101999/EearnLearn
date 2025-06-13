package com.earnlearn.controller;

import com.earnlearn.model.Order;
import com.earnlearn.model.Role;
import com.earnlearn.model.User;
import com.earnlearn.repository.CourseRepository;
import com.earnlearn.repository.ProductRepository;
import com.earnlearn.service.OrderService;
import com.earnlearn.service.RoleService;
import com.earnlearn.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
	private final UserService userService;
	private final RoleService roleService;
	private final OrderService orderService;
	private final CourseRepository courseRepository;
	private final ProductRepository productRepository;

	public AdminController(UserService userService, RoleService roleService, OrderService orderService,
			CourseRepository courseRepository, ProductRepository productRepository) {
		this.userService = userService;
		this.roleService = roleService;
		this.orderService = orderService;
		this.courseRepository = courseRepository;
		this.productRepository = productRepository;
	}

	@GetMapping("/dashboard")
	public String adminDashboard(Model model) {
		model.addAttribute("pageTitle", "Admin Dashboard");
		long totalUsers = userService.countTotalUsers();
		model.addAttribute("totalUsers", totalUsers);
		long totalCourses = courseRepository.count();
		model.addAttribute("totalCourses", totalCourses);
		long totalProducts = productRepository.count();
		model.addAttribute("totalProducts", totalProducts);
		return "admin/dashboard";
	}

	@GetMapping("/users")
	public String listUsers(Model model) {
		List<User> users = userService.findAllUsers();
		model.addAttribute("users", users);
		model.addAttribute("pageTitle", "Manage Users");
		return "admin/user-list";
	}

	@GetMapping("/users/edit/{id}")
	public String showEditUserForm(@PathVariable Long id, Model model) {
		User user = userService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
		List<Role> allRoles = roleService.findAll();
		Set<String> userRoleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
		model.addAttribute("user", user);
		model.addAttribute("allRoles", allRoles);
		model.addAttribute("userRoleNames", userRoleNames); // For pre-selecting roles in the form
		model.addAttribute("pageTitle", "Edit User");
		return "admin/user-edit";
	}

	@PostMapping("/users/update/{id}")
	public String updateUser(@PathVariable Long id, @ModelAttribute User user, // Basic details like name, email
			@RequestParam(required = false) Set<String> roleNames, RedirectAttributes redirectAttributes) {
		try {
			// Ensure the ID from the path is set to the user object
			user.setId(id);
			userService.adminUpdateUser(user, roleNames); // New method in UserService for admin updates
			redirectAttributes.addFlashAttribute("successMessage", "User updated successfully.");
			return "redirect:/admin/users";
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/admin/users/edit/" + id;
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
			return "redirect:/admin/users/edit/" + id;
		}
	}

	@GetMapping("/users/delete/{id}")
	public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		try {
			userService.deleteUserById(id);
			redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully.");
		} catch (IllegalArgumentException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Could not delete user. They might have associated data.");
		}
		return "redirect:/admin/users";
	}

	// ADMIN ORDER MANAGEMENT ---
	@GetMapping("/orders")
	public String listAllOrders(Model model) {
		model.addAttribute("orders", orderService.getAllOrders());
		model.addAttribute("pageTitle", "Manage Orders");
		return "admin/order-list";
	}

	@GetMapping("/orders/view/{id}")
	public String viewOrderDetails(@PathVariable Long id, Model model) {
		model.addAttribute("order", orderService.getOrderById(id));
		model.addAttribute("statuses", Order.OrderStatus.values()); // For the dropdown
		model.addAttribute("pageTitle", "Order Details");
		return "admin/order-details";
	}

	@PostMapping("/orders/update/{id}")
	public String updateOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status,
			@RequestParam(required = false) String trackingNumber, RedirectAttributes redirectAttributes) {
		try {
			orderService.updateOrderStatus(id, status, trackingNumber);
			redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating order status: " + e.getMessage());
		}
		return "redirect:/admin/orders/view/" + id;
	}
}
