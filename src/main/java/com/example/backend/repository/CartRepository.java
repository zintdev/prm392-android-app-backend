package com.example.backend.repository;

import com.example.backend.domain.enums.CartStatus;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Integer> {
  Optional<Cart> findByUserIdAndStatus(Integer userId, CartStatus status);
}