package com.earnlearn.repository;

import com.earnlearn.model.Order;
import com.earnlearn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
	List<Order> findByUser(User user);
}