package com.earnlearn.service;

import com.earnlearn.model.*;
import com.earnlearn.repository.ReviewRepository;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
	private final ReviewRepository reviewRepository;
	private final ProductService productService;
	private final UserService userService;

	public ReviewService(ReviewRepository reviewRepository, ProductService productService, UserService userService) {
		this.reviewRepository = reviewRepository;
		this.productService = productService;
		this.userService = userService;
	}

	@Transactional
	public Review createReview(Review review, Long productId, String username) {
		User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
		Product product = productService.getProductById(productId);

		if (reviewRepository.existsByUserAndProduct(user, product)) {
			throw new IllegalStateException(
					"You've already reviewed this product. " + "Update your existing review instead.");
		}

		review.setUser(user);
		review.setProduct(product);
		review.setCreatedAt(LocalDateTime.now());

		Review savedReview = reviewRepository.save(review);
		updateProductAverageRating(product);
		return savedReview;
	}

	private void updateProductAverageRating(Product product) {
		Double average = reviewRepository.findAverageRatingByProductId(product.getId()).orElse(0.0);

		// Round to 1 decimal place manually
		double roundedAverage = Math.round(average * 10) / 10.0;

		product.setAverageRating(roundedAverage);
		productService.saveProduct(product);
	}

	public List<Review> getProductReviews(Long productId) {
		return reviewRepository.findByProduct(productService.getProductById(productId));
	}

	public Review getReviewById(Long reviewId) {
		return reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found with id: " + reviewId));
	}

	public void updateReview(Long reviewId, Review updatedReview, String username) {
		Review existingReview = reviewRepository.findById(reviewId)
				.orElseThrow(() -> new RuntimeException("Review not found"));

		User user = userService.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));

		if (!existingReview.getUser().getId().equals(user.getId())) {
			throw new AccessDeniedException("You don't own this review");
		}

		existingReview.setRating(updatedReview.getRating());
		existingReview.setComment(updatedReview.getComment());
		reviewRepository.save(existingReview);

		// Update product rating
		updateProductAverageRating(existingReview.getProduct());
	}
}