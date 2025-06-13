package com.earnlearn.controller;

import com.earnlearn.model.Payment;
import com.earnlearn.model.User;
import com.earnlearn.service.ActivityService;
import com.earnlearn.service.CartService;
import com.earnlearn.service.PaymentService;
import com.earnlearn.service.QRCodeService;
import com.earnlearn.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/payments")
public class PaymentController {

	@Autowired
	private ActivityService activityService;

	@Autowired
	private UserService userService;

	@Autowired
	private CartService cartService;

	private final PaymentService paymentService;
	private final QRCodeService qrCodeService;

	// Constructor injection for services
	public PaymentController(PaymentService paymentService, QRCodeService qrCodeService) {
		this.paymentService = paymentService;
		this.qrCodeService = qrCodeService;
	}

	@GetMapping("/process")
	public String showPaymentPage(@RequestParam String transactionId, Model model,
			RedirectAttributes redirectAttributes, @AuthenticationPrincipal UserDetails userDetails) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		Payment payment = paymentService.getPaymentByTransactionId(transactionId);

		if (payment == null) {
			redirectAttributes.addFlashAttribute("errorMessage", "Payment not found for transaction: " + transactionId);
			return "redirect:/cart";
		}

		// Ensure the payment belongs to the current user (security check)
		if (!payment.getUser().getId().equals(user.getId())) {
			redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized payment access attempt.");
			return "redirect:/cart";
		}

		// This check ensures that if an expired payment is accessed via GET,
		// it's marked as failed and the user is redirected.
		if (payment.getStatus() == Payment.PaymentStatus.PENDING && payment.getExpiresAt() != null
				&& LocalDateTime.now().isAfter(payment.getExpiresAt())) {
			try {
				paymentService.failPayment(payment.getId()); // Mark as failed due to expiry
			} catch (Exception e) {
				// Log if failing payment also throws an error
				System.err.println("Error marking payment as failed on expiry: " + e.getMessage());
			}
			redirectAttributes.addFlashAttribute("errorMessage",
					"Payment session has expired. Please try placing the order again.");
			return "redirect:/cart";
		}

		model.addAttribute("payment", payment);
		model.addAttribute("qrCode", qrCodeService.generatePaymentQR(payment)); // Pass QR code URL to the template
		return "payment/checkout"; // Return the payment checkout Thymeleaf template
	}

	@PostMapping("/confirm/{transactionId}")
	public String confirmPayment(@PathVariable String transactionId, // Using @PathVariable as per your HTML form action
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		User user = userService.findByUsername(userDetails.getUsername())
				.orElseThrow(() -> new RuntimeException("User not found"));
		try {
			Payment payment = paymentService.getPaymentByTransactionId(transactionId);
			if (payment == null) {
				redirectAttributes.addFlashAttribute("errorMessage",
						"Payment transaction not found for confirmation: " + transactionId);
				return "redirect:/cart";
			}
			// Ensure the payment belongs to the current user (security check)
			if (!payment.getUser().getId().equals(user.getId())) {
				redirectAttributes.addFlashAttribute("errorMessage", "Unauthorized payment confirmation attempt.");
				return "redirect:/cart";
			}

			paymentService.confirmPayment(payment.getId());// This will update payment status and order status to PROCESSING
			cartService.clearCart(user); // Clear the user's cart

			// Log activity
			String description = "You completed a purchase of ₹" + payment.getAmount();
			String link = "/orders"; // A generic link to the orders page
			activityService.createActivity(user, "Purchase", description, link);

			redirectAttributes.addFlashAttribute("successMessage",
					"Payment confirmed successfully! Your order is being processed.");
			return "redirect:/orders/" + payment.getOrder().getId(); // Redirect to the order details page
		} catch (IllegalStateException e) { 
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); 
			return "redirect:/cart";
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error confirming payment: " + e.getMessage());
			return "redirect:/cart";
		}
	}
}
