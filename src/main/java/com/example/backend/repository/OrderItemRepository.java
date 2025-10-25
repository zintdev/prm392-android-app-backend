package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.OrderItem;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {}