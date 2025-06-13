package com.earnlearn.service;

import com.earnlearn.model.Order;
import com.earnlearn.model.Payment;
import com.earnlearn.model.User;
import com.earnlearn.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID; // For generating simulated transaction IDs

@Service
public class PaymentService {
	@Autowired
	private PaymentRepository paymentRepo;
	@Autowired
	private OrderService orderService;

	@Transactional
	public Payment createPayment(User user, Order order, Double amount) {
		String transactionId = UUID.randomUUID().toString(); // Simulate unique transaction ID
		Payment payment = new Payment();
		payment.setUser(user);
		payment.setOrder(order);
		payment.setAmount(amount);
		payment.setPaymentDate(LocalDateTime.now());
		payment.setStatus(Payment.PaymentStatus.PENDING); // Initial status is PENDING
		payment.setTransactionId(transactionId);
		payment.setExpiresAt(LocalDateTime.now().plusMinutes(15)); // Payment expires in 15 minutes
		return paymentRepo.save(payment);
	}

	@Transactional
	public Payment confirmPayment(Long paymentId) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new IllegalStateException("Payment not found with ID: " + paymentId));

		// Prevent confirmation if already completed or refunded
		if (payment.getStatus() == Payment.PaymentStatus.COMPLETED
				|| payment.getStatus() == Payment.PaymentStatus.REFUNDED) {
			System.out.println("Payment " + paymentId + " is already " + payment.getStatus() + ". No action taken.");
			return payment;
		}

		// Check for expiry if payment is PENDING
		if (payment.getStatus() == Payment.PaymentStatus.PENDING && payment.getExpiresAt() != null
				&& LocalDateTime.now().isAfter(payment.getExpiresAt())) {
			payment.setStatus(Payment.PaymentStatus.FAILED); // Mark as failed due to expiry
			paymentRepo.save(payment);
			throw new IllegalStateException("Payment session has expired.");
		}

		// Proceed with confirmation if PENDING and not expired, or if it was previously
		// FAILED (allowing retry)
		payment.setStatus(Payment.PaymentStatus.COMPLETED);
		payment.setPaymentDate(LocalDateTime.now()); // Update timestamp for confirmation
		paymentRepo.save(payment);

		// Update the status of the associated order
		Order order = payment.getOrder();
		if (order == null) {
			throw new IllegalStateException("Order not associated with payment " + paymentId);
		}
		orderService.updateOrderStatus(order.getId(), Order.OrderStatus.PROCESSING, null); // Change order status to PROCESSING
		return payment;
	}

	@Transactional
	public Payment failPayment(Long paymentId) {
		Payment payment = paymentRepo.findById(paymentId)
				.orElseThrow(() -> new IllegalStateException("Payment not found with ID: " + paymentId));
		payment.setStatus(Payment.PaymentStatus.FAILED);
		return paymentRepo.save(payment);
	}

	public Payment getPaymentByTransactionId(String transactionId) {
		return paymentRepo.findByTransactionId(transactionId).orElse(null);
	}

	public Payment getPaymentById(Long id) {
		return paymentRepo.findById(id)
				.orElseThrow(() -> new IllegalStateException("Payment not found with ID: " + id));
	}
}
