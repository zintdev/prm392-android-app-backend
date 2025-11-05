package com.example.backend.repository;

import com.example.backend.domain.entity.StoreLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoreRepository extends JpaRepository<StoreLocation, Integer> {
    boolean existsByStoreNameIgnoreCase(String storeName);
    boolean existsByAddressIgnoreCase(String address);

    // 1) Nearby không cần product
    @Query(value = """
        SELECT 
            s.store_location_id AS storeId,
            s.store_name AS name,
            s.address AS address,
            CAST(s.latitude AS DOUBLE PRECISION) AS latitude,
            CAST(s.longitude AS DOUBLE PRECISION) AS longitude,
            (earth_distance(
                ll_to_earth(:lat, :lng),
                ll_to_earth(CAST(s.latitude AS DOUBLE PRECISION),
                            CAST(s.longitude AS DOUBLE PRECISION))
            ) / 1000.0) AS distanceKm
        FROM store_locations s
        WHERE (:radiusKm IS NULL OR earth_distance(
                 ll_to_earth(:lat, :lng),
                 ll_to_earth(CAST(s.latitude AS DOUBLE PRECISION),
                             CAST(s.longitude AS DOUBLE PRECISION))
              ) <= (:radiusKm * 1000.0))
        ORDER BY distanceKm ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearby(@Param("lat") double lat,
                              @Param("lng") double lng,
                              @Param("radiusKm") Double radiusKm,
                              @Param("limit") int limit);

    // 2) Nearby cho 1 product, có thể lọc inStockOnly (qty >=1)
    @Query(value = """
        SELECT 
            s.store_location_id AS storeId,
            s.store_name AS name,
            s.address AS address,
            CAST(s.latitude AS DOUBLE PRECISION) AS latitude,
            CAST(s.longitude AS DOUBLE PRECISION) AS longitude,
            (earth_distance(
                ll_to_earth(:lat, :lng),
                ll_to_earth(CAST(s.latitude AS DOUBLE PRECISION),
                            CAST(s.longitude AS DOUBLE PRECISION))
            ) / 1000.0) AS distanceKm,
            si.quantity AS quantity
        FROM store_locations s
        JOIN store_inventory si 
          ON si.store_location_id = s.store_location_id 
         AND si.product_id = :productId
        WHERE (:inStockOnly = FALSE OR si.quantity >= 1)
          AND (:radiusKm IS NULL OR earth_distance(
                 ll_to_earth(:lat, :lng),
                 ll_to_earth(CAST(s.latitude AS DOUBLE PRECISION),
                             CAST(s.longitude AS DOUBLE PRECISION))
              ) <= (:radiusKm * 1000.0))
        ORDER BY distanceKm ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findNearbyWithInventory(@Param("lat") double lat,
                                           @Param("lng") double lng,
                                           @Param("radiusKm") Double radiusKm,
                                           @Param("limit") int limit,
                                           @Param("productId") int productId,
                                           @Param("inStockOnly") boolean inStockOnly);
}
