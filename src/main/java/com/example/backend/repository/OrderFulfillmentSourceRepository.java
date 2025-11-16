package com.example.backend.repository;

import com.example.backend.domain.entity.OrderFulfillmentSource;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderFulfillmentSourceRepository extends JpaRepository<OrderFulfillmentSource, Integer> {

    List<OrderFulfillmentSource> findByOrderId(Integer orderId);

    @Query("""
        SELECT COALESCE(SUM(ofs.softReservedQuantity - ofs.hardDeductedQuantity), 0)
        FROM OrderFulfillmentSource ofs
        WHERE ofs.storeLocation.id = :storeLocationId
          AND ofs.product.id = :productId
    """)
    Integer sumPendingReservedQuantity(@Param("storeLocationId") Integer storeLocationId,
                                       @Param("productId") Integer productId);
}
