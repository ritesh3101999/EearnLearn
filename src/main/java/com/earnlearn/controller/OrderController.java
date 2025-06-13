package com.earnlearn.controller;

import com.earnlearn.model.*;
import com.earnlearn.service.CartService;
import com.earnlearn.service.OrderService;
import com.earnlearn.service.PaymentService;
import com.earnlearn.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@Autowired
	private UserService userService;

	@Autowired
	private PaymentService paymentService;

	@Autowired
	private CartService cartService;

	@GetMapping("/checkout/details")
	public String showShippingDetailsForm(Model model, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Cart cart = cartService.getOrCreateCart(user);

		if (cart.getItems().isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Your cart is empty. Please add items before checking out.");
			return "redirect:/cart";
		}

		model.addAttribute("order", new Order()); // Use a new order object to bind form fields
		model.addAttribute("cart", cart); // Pass cart details to display summary
		model.addAttribute("cartTotal", cartService.getCartTotal(user));
		return "orders/shipping-details"; // New Thymeleaf template for shipping details
	}

	@PostMapping("/place")
	public String placeOrder(@Valid @ModelAttribute Order order, BindingResult bindingResult,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// If there are validation errors, return to the form
		if (bindingResult.hasErrors()) {
			// Add cart and cart total back to the model for redisplay
			Cart cart = cartService.getOrCreateCart(user);
			redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.order", bindingResult);
			redirectAttributes.addFlashAttribute("order", order); // Pass the order object back to retain input
			redirectAttributes.addFlashAttribute("cart", cart);
			redirectAttributes.addFlashAttribute("cartTotal", cartService.getCartTotal(user));
			redirectAttributes.addFlashAttribute("errorMessage", "Please correct the errors in shipping details.");
			return "redirect:/orders/checkout/details"; // Redirect back to the form
		}

		try {
			// Create the order with shipping details
			Order placedOrder = orderService.createOrderFromCart(user, order.getShippingAddress(),
					order.getShippingCity(), order.getShippingState(), order.getShippingZip());

			// Initiate payment for the placed order
			Double total = cartService.getCartTotal(user); // Get total from the cart before it's cleared
			// Pass the 'placedOrder' object as the second argument
			Payment payment = paymentService.createPayment(user, placedOrder, total);

			redirectAttributes.addAttribute("transactionId", payment.getTransactionId());
			redirectAttributes.addFlashAttribute("successMessage", "Order placed! Proceeding to payment.");
			return "redirect:/payments/process"; // Redirect to payment processing endpoint
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
			return "redirect:/cart"; // If cart becomes empty unexpectedly or other business logic error
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error placing order: " + e.getMessage());
			return "redirect:/orders/checkout/details"; // Or a generic error page
		}
	}

	@GetMapping("/{id}")
	public String viewOrder(@PathVariable Long id, Model model) {
		Order order = orderService.getOrderById(id);
		model.addAttribute("order", order);
		return "orders/details";
	}

	@GetMapping
	public String listOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		model.addAttribute("orders", orderService.getOrdersByUser(user));
		return "orders/list";
	}

	@GetMapping("/seller")
	public String listSellerOrders(Model model, @AuthenticationPrincipal UserDetails userDetails) {
		User seller = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Seller not found"));
		model.addAttribute("orders", orderService.getOrdersForSeller(seller));
		return "orders/seller-list"; // New template for seller's order list
	}

	@GetMapping("/seller/view/{id}")
	public String viewSellerOrderDetails(@PathVariable Long id, Model model,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User seller = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Seller not found"));
		Order order = orderService.getOrderById(id);

		// Verify if the order contains at least one product from this seller
		boolean containsSellerProduct = order.getItems().stream()
				.anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));

		if (!containsSellerProduct) {
			redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to view this order.");
			return "redirect:/orders/seller";
		}

		model.addAttribute("order", order);
		model.addAttribute("statuses", Order.OrderStatus.values()); // For status dropdown
		return "orders/seller-details"; // New template for seller's order details
	}

	@PostMapping("/seller/update/{id}")
	public String updateSellerOrderStatus(@PathVariable Long id, @RequestParam Order.OrderStatus status,
			@RequestParam(required = false) String trackingNumber, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User seller = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("Seller not found"));
		Order order = orderService.getOrderById(id);

		// Verify if the order contains at least one product from this seller
		boolean containsSellerProduct = order.getItems().stream()
				.anyMatch(item -> item.getProduct().getSeller().getId().equals(seller.getId()));

		if (!containsSellerProduct) {
			redirectAttributes.addFlashAttribute("errorMessage", "You are not authorized to update this order.");
			return "redirect:/orders/seller";
		}

		try {
			orderService.updateOrderStatus(id, status, trackingNumber);
			redirectAttributes.addFlashAttribute("successMessage", "Order status updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating order status: " + e.getMessage());
		}
		return "redirect:/orders/seller/view/" + id;
	}
}
