package com.earnlearn.repository;

import com.earnlearn.model.Cart;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
	Optional<Cart> findByUser(User user);
}