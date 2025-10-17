package com.example.backend.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {}