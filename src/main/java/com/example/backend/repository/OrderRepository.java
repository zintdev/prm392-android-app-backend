package com.example.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Sort;

import com.example.backend.domain.entity.Order;
import com.example.backend.domain.enums.OrderStatus;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUserId(Integer userId);
    List<Order> findByOrderStatus(OrderStatus orderStatus, Sort sort);
    List<Order> findByUserIdAndOrderStatus(Integer userId, OrderStatus status);
}
