package com.earnlearn.model;

import jakarta.persistence.*;

@Entity
@Table(name = "order_items")
public class OrderItem {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	private int quantity;
	private double priceAtPurchase;

	@ManyToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	// Getters and Setters
	public Long getId() {
		return id;
	}

	public Product getProduct() {
		return product;
	}

	public int getQuantity() {
		return quantity;
	}

	public double getPriceAtPurchase() {
		return priceAtPurchase;
	}

	public Order getOrder() {
		return order;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public void setPriceAtPurchase(double priceAtPurchase) {
		this.priceAtPurchase = priceAtPurchase;
	}

	public void setOrder(Order order) {
		this.order = order;
	}
}