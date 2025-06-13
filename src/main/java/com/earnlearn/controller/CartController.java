package com.earnlearn.controller;

import com.earnlearn.model.*;
import com.earnlearn.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

	@Autowired
	private CartService cartService;

	@Autowired
	private UserService userService;

	// View Cart
	@GetMapping
	public String viewCart(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Cart cart = cartService.getOrCreateCart(user);
		int itemCount = cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
		model.addAttribute("cart", cart);
		request.getSession().setAttribute("cartCount", itemCount);
		return "cart/view";
	}

	// Add to Cart (from product page)
	@PostMapping("/add/{productId}")
	public String addToCart(@PathVariable Long productId, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes, HttpServletRequest request) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		cartService.addToCart(user, productId);
		// Add success message
		redirectAttributes.addFlashAttribute("successMessage", "Item added to cart!");
		int newCount = cartService.getCartItemCount(user);
		request.getSession().setAttribute("cartCount", newCount);
		return "redirect:/products/" + productId; // Stay on product page
	}

	// Remove Item from Cart
	@PostMapping("/remove/{itemId}")
	public String removeFromCart(@PathVariable Long itemId, @AuthenticationPrincipal UserDetails userDetails,
			HttpServletRequest request, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		cartService.removeFromCart(user, itemId);
		redirectAttributes.addFlashAttribute("successMessage", "Item removed from cart!");
		int newCount = cartService.getCartItemCount(user);
		request.getSession().setAttribute("cartCount", newCount);
		return "redirect:/cart"; // Refresh cart page
	}

	// Clear Entire Cart
	@PostMapping("/clear")
	public String clearCart(@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		cartService.clearCart(user);
		redirectAttributes.addFlashAttribute("successMessage", "Cart cleared successfully!");
		return "redirect:/cart";
	}

	// Redirect to shipping details form instead of direct payment
	@PostMapping("/checkout")
	public String initiateCheckout(@AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		// Check if cart is empty before proceeding
		if (cartService.getOrCreateCart(user).getItems().isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessage",
					"Your cart is empty. Please add items before checking out.");
			return "redirect:/cart";
		}

		redirectAttributes.addFlashAttribute("successMessage", "Please provide shipping details.");
		return "redirect:/orders/checkout/details"; // Redirect to new shipping details page
	}

	@PostMapping("/update/{itemId}")
	public String updateItemQuantity(@PathVariable Long itemId, @RequestParam int quantity,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));

		cartService.updateItemQuantity(user, itemId, quantity);
		redirectAttributes.addFlashAttribute("successMessage", "Cart updated!");
		return "redirect:/cart";
	}
}
