package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.StoreInventory;

import java.util.List;
import java.util.Optional;

public interface StoreInventoryRepository extends JpaRepository<StoreInventory, Integer> {

    @Query(value = """
        SELECT COALESCE(SUM(si.quantity), 0)
        FROM store_inventory si
        WHERE si.product_id = :productId
    """, nativeQuery = true)
    Integer sumQuantityByProductId(@Param("productId") Integer productId);

    Optional<StoreInventory> findByStoreLocationIdAndProductId(Integer storeLocationId, Integer productId);

    List<StoreInventory> findByStoreLocationId(Integer storeLocationId);

    @Query(value = """
        SELECT product_id, COALESCE(SUM(quantity), 0)
        FROM store_inventory
        GROUP BY product_id
    """, nativeQuery = true)
    List<Object[]> sumQuantityGroupedByProductId();

    @Query("""
        SELECT si FROM StoreInventory si
        JOIN FETCH si.storeLocation sl
        WHERE si.product.id = :productId
          AND (:excludeStoreId IS NULL OR sl.id <> :excludeStoreId)
        ORDER BY si.quantity DESC
    """)
    List<StoreInventory> findAllByProductIdOrderByQuantityDesc(@Param("productId") Integer productId,
                                                               @Param("excludeStoreId") Integer excludeStoreId);

}
