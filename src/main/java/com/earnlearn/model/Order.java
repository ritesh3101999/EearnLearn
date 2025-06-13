package com.earnlearn.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {
	public enum OrderStatus {
		PENDING, // Order created, awaiting payment confirmation
		PROCESSING, // Payment confirmed, order is being prepared
		SHIPPED, // Order has been shipped
		DELIVERED, // Order has been delivered
		CANCELLED // Order has been cancelled
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	private LocalDateTime orderDate;

	@Enumerated(EnumType.STRING)
	private OrderStatus status;

	// SHIPPING INFORMATION --- Added validation annotations
	@NotBlank(message = "Shipping address is required")
	@Size(max = 255, message = "Shipping address cannot exceed 255 characters")
	private String shippingAddress;

	@NotBlank(message = "Shipping city is required")
	@Size(max = 100, message = "Shipping city cannot exceed 100 characters")
	private String shippingCity;

	@NotBlank(message = "Shipping state is required")
	@Size(max = 100, message = "Shipping state cannot exceed 100 characters")
	private String shippingState;

	@NotBlank(message = "Shipping ZIP code is required")
	@Size(max = 20, message = "Shipping ZIP code cannot exceed 20 characters")
	private String shippingZip;

	private String trackingNumber; // For shipped orders

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public OrderStatus getStatus() {
		return status;
	}

	public void setStatus(OrderStatus status) {
		this.status = status;
	}

	public List<OrderItem> getItems() {
		return items;
	}

	public void setItems(List<OrderItem> items) {
		this.items = items;
	}

	public String getShippingAddress() {
		return shippingAddress;
	}

	public void setShippingAddress(String shippingAddress) {
		this.shippingAddress = shippingAddress;
	}

	public String getShippingCity() {
		return shippingCity;
	}

	public void setShippingCity(String shippingCity) {
		this.shippingCity = shippingCity;
	}

	public String getShippingState() {
		return shippingState;
	}

	public void setShippingState(String shippingState) {
		this.shippingState = shippingState;
	}

	public String getShippingZip() {
		return shippingZip;
	}

	public void setShippingZip(String shippingZip) {
		this.shippingZip = shippingZip;
	}

	public String getTrackingNumber() {
		return trackingNumber;
	}

	public void setTrackingNumber(String trackingNumber) {
		this.trackingNumber = trackingNumber;
	}

	/**
	 * Helper method to calculate the total quantity of items in this order. This
	 * avoids complex Stream API expressions directly in the Thymeleaf template.
	 * 
	 * @return The sum of quantities of all OrderItems.
	 */
	public int getTotalQuantity() {
		return this.items.stream().mapToInt(OrderItem::getQuantity).sum();
	}
}
