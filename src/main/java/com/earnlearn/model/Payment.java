package com.earnlearn.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {

	public enum PaymentStatus {
		PENDING, // Payment initiated, awaiting confirmation
		COMPLETED, // Payment successful
		FAILED, // Payment failed
		REFUNDED // Payment refunded
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@OneToOne // Each payment is for one specific order in this flow
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	private Double amount;

	private LocalDateTime paymentDate;

	@Enumerated(EnumType.STRING)
	private PaymentStatus status;

	private String transactionId; // Unique ID from payment gateway (simulated)

	private LocalDateTime expiresAt;

	// Constructors
	public Payment() {
	}

	public Payment(User user, Order order, Double amount, String transactionId, PaymentStatus status) {
		this.user = user;
		this.order = order;
		this.amount = amount;
		this.transactionId = transactionId;
		this.paymentDate = LocalDateTime.now();
		this.status = status;
	}

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

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
	}

	public LocalDateTime getExpiresAt() {
		return expiresAt;
	}

	public void setExpiresAt(LocalDateTime expiresAt) {
		this.expiresAt = expiresAt;
	}

}
