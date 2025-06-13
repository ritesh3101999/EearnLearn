package com.earnlearn.repository;

import com.earnlearn.model.Product;
import com.earnlearn.model.Review;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
	List<Review> findByProduct(Product product);

	boolean existsByUserAndProduct(User user, Product product);

	@Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
	Optional<Double> findAverageRatingByProductId(@Param("productId") Long productId);

	Optional<Review> findById(Long id);
}