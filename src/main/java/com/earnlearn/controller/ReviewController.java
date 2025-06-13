package com.earnlearn.controller;

import com.earnlearn.model.Product;
import com.earnlearn.model.Review;
import com.earnlearn.service.ProductService;
import com.earnlearn.service.ReviewService;
import java.util.List;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products/{productId}/reviews")
public class ReviewController {
	private final ReviewService reviewService;
	private final ProductService productService;

	public ReviewController(ReviewService reviewService, ProductService productService) {
		this.reviewService = reviewService;
		this.productService = productService;
	}

	@GetMapping
	public String showReviewsPage(@PathVariable Long productId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {

		Product product = productService.getProductById(productId);
		List<Review> reviews = reviewService.getProductReviews(productId);

		model.addAttribute("product", product);
		model.addAttribute("reviews", reviews);

		// Add empty review object for form
		if (userDetails != null) {
			model.addAttribute("newReview", new Review());
		}

		return "products/review";
	}

	@PostMapping
	public String submitReview(@PathVariable Long productId, @ModelAttribute Review review,
			@AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {
		try {
			reviewService.createReview(review, productId, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("successMessage", "Review submitted successfully!");
		} catch (IllegalStateException e) {
			redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
		}
		return "redirect:/products/" + productId;
	}

	@ExceptionHandler(IllegalStateException.class)
	public String handleDuplicateReview(IllegalStateException ex, RedirectAttributes redirectAttributes) {
		redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
		return "redirect:/products/{productId}";
	}

	// Add these methods for edit functionality
	@GetMapping("/edit/{reviewId}")
	public String showEditForm(@PathVariable Long productId, @PathVariable Long reviewId, Model model,
			@AuthenticationPrincipal UserDetails userDetails) {
		Review review = reviewService.getReviewById(reviewId);
		Product product = productService.getProductById(productId);
		// Security check
		if (!review.getUser().getUsername().equals(userDetails.getUsername())) {
			throw new AccessDeniedException("You cannot edit this review");
		}
		model.addAttribute("product", product);
		model.addAttribute("review", review);
		return "products/review-edit";
	}

	@PostMapping("/update/{reviewId}")
	public String updateReview(@PathVariable Long productId, @PathVariable Long reviewId,
			@ModelAttribute("review") Review updatedReview, @AuthenticationPrincipal UserDetails userDetails,
			RedirectAttributes redirectAttributes) {
		try {
			reviewService.updateReview(reviewId, updatedReview, userDetails.getUsername());
			redirectAttributes.addFlashAttribute("successMessage", "Review updated successfully!");
		} catch (Exception e) {
			redirectAttributes.addFlashAttribute("errorMessage", "Error updating review: " + e.getMessage());
		}
		return "redirect:/products/" + productId;
	}
}
