package com.earnlearn.repository;

import com.earnlearn.model.Product;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findBySeller(User seller);

	List<Product> findByCategory(String category);

	List<Product> findByNameContaining(String name);

	@Query("SELECT p FROM Product p LEFT JOIN FETCH p.lectures WHERE p.id = :id")
	Optional<Product> findByIdWithLectures(@Param("id") Long id);
}