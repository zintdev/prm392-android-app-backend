package com.example.backend.repository;

import java.math.BigDecimal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.StoreInventory;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Integer> {

    @Query(value = """
        SELECT COALESCE(SUM(si.quantity), 0)
        FROM store_inventory si
        WHERE si.product_id = :productId
    """, nativeQuery = true)
    Integer sumQuantityByProductId(@Param("productId") Integer productId);

}
