package com.earnlearn.repository;

import com.earnlearn.model.Cart;
import com.earnlearn.model.CartItem;
import com.earnlearn.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
	Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
	
	void deleteAllByCart(Cart cart);
}