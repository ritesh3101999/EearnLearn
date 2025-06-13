package com.earnlearn.service;

import com.earnlearn.model.*;
import com.earnlearn.repository.*;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class CartService {
	@Autowired
	private CartRepository cartRepo;
	@Autowired
	private CartItemRepository cartItemRepo;
	@Autowired
	private ProductRepository productRepo;

	// Get or create cart for user
	public Cart getOrCreateCart(User user) {
		return cartRepo.findByUser(user).orElseGet(() -> {
			Cart newCart = new Cart();
			newCart.setUser(user);
			return cartRepo.save(newCart);
		});
	}

	// Add product to cart
	@Transactional
	public void addToCart(User user, Long productId) {
		Product product = productRepo.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));
		Cart cart = getOrCreateCart(user);

		Optional<CartItem> existingItem = cartItemRepo.findByCartAndProduct(cart, product);
		if (existingItem.isPresent()) {
			CartItem item = existingItem.get();
			item.setQuantity(item.getQuantity() + 1);
			cartItemRepo.save(item);
		} else {
			CartItem newItem = new CartItem();
			newItem.setProduct(product);
			newItem.setQuantity(1);
			newItem.setCart(cart);
			cartItemRepo.save(newItem);
		}
	}

	// Remove item from cart
	public void removeFromCart(User user, Long itemId) {
		cartItemRepo.deleteById(itemId); // No need to fetch cart
	}

	// Clear cart
	@Transactional // Ensures the database operation runs within a transaction
	public void clearCart(User user) {
		cartItemRepo.deleteAllByCart(getOrCreateCart(user)); // Directly use cart
	}

	public int getCartItemCount(User user) {
		Cart cart = getOrCreateCart(user);
		return cart.getItems().stream().mapToInt(CartItem::getQuantity).sum();
	}

	// Calculate cart total
	public Double getCartTotal(User user) {
		return cartRepo.findByUser(user).map(cart -> cart.getItems().stream()
				.mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity()).sum()).orElse(0.0);
	}

	@Transactional
	public void updateItemQuantity(User user, Long itemId, int quantity) {
		CartItem item = cartItemRepo.findById(itemId).orElseThrow(() -> new RuntimeException("Item not found"));

		if (item.getCart().getUser().getId().equals(user.getId())) {
			if (quantity <= 0) {
				cartItemRepo.delete(item);
			} else {
				item.setQuantity(quantity);
				cartItemRepo.save(item);
			}
		} else {
			throw new SecurityException("Unauthorized item update");
		}
	}
}