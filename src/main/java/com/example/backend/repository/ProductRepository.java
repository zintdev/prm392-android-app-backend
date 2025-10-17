package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {

     @Query("SELECT p.quantity FROM Product p WHERE p.id = :productId")
    Integer findQuantityById(@Param("productId") Integer productId);
}
