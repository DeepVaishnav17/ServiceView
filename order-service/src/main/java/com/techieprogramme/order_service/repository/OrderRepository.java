package com.techieprogramme.order_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.techieprogramme.order_service.model.Order;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsername(String username);
}
