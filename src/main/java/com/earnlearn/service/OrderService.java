package com.earnlearn.service;

import com.earnlearn.model.*;
import com.earnlearn.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

	@Autowired
	private OrderRepository orderRepo;

	@Autowired
	private OrderItemRepository orderItemRepo;

	@Autowired
	private CartService cartService;

	@Autowired
	private ProductRepository productRepository;

	@Transactional
	public Order createOrderFromCart(User user, String shippingAddress, String shippingCity, String shippingState,
			String shippingZip) {
		Cart cart = cartService.getOrCreateCart(user);
		if (cart.getItems().isEmpty()) {
			throw new IllegalStateException("Cannot create an order from an empty cart.");
		}

		Order order = new Order();
		order.setUser(user);
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(Order.OrderStatus.PENDING); // Initial status

		// Set the shipping details provided by the user
		order.setShippingAddress(shippingAddress);
		order.setShippingCity(shippingCity);
		order.setShippingState(shippingState);
		order.setShippingZip(shippingZip);

		orderRepo.save(order); // Save order first to get an ID

		cart.getItems().forEach(cartItem -> {
			OrderItem orderItem = new OrderItem();
			orderItem.setProduct(cartItem.getProduct());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setPriceAtPurchase(cartItem.getProduct().getPrice());
			orderItem.setOrder(order); // Link item to the saved order
			orderItemRepo.save(orderItem);
		});

		cartService.clearCart(user); // Clear cart after creating the order
		return order;
	}

	public List<Order> getAllOrders() {
		return orderRepo.findAll();
	}

	public List<Order> getOrdersByUser(User user) {
		return orderRepo.findByUser(user);
	}

	public Order getOrderById(Long id) {
		return orderRepo.findById(id).orElseThrow(() -> new RuntimeException("Order not found with ID: " + id));
	}

	@Transactional
	public Order updateOrderStatus(Long orderId, Order.OrderStatus newStatus, String trackingNumber) {
		Order order = getOrderById(orderId);
		order.setStatus(newStatus);
		if (newStatus == Order.OrderStatus.SHIPPED && trackingNumber != null && !trackingNumber.isEmpty()) {
			order.setTrackingNumber(trackingNumber);
		} else if (newStatus != Order.OrderStatus.SHIPPED) {
			// Clear tracking number if status is not SHIPPED
			order.setTrackingNumber(null);
		}
		return orderRepo.save(order);
	}

	public List<Order> getOrdersForSeller(User seller) {
		// Get all products sold by this seller
		List<Product> sellerProducts = productRepository.findBySeller(seller);
		List<Long> sellerProductIds = sellerProducts.stream().map(Product::getId).collect(Collectors.toList());

		// Find all orders and filter them if any order item belongs to the seller's
		// products
		return orderRepo.findAll().stream()
				.filter(order -> order.getItems().stream()
						.anyMatch(item -> sellerProductIds.contains(item.getProduct().getId())))
				.collect(Collectors.toList());
	}
}
