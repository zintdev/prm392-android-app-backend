package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.backend.domain.entity.Product;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {

	@Query("SELECT p.quantity FROM Product p WHERE p.id = :productId")
	Integer findQuantityById(@Param("productId") Integer productId);

	List<Product> findByNameContainingIgnoreCase(String name);
}
