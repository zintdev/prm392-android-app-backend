package com.example.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Integer> {}
