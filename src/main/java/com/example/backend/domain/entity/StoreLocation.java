package com.example.backend.domain.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "store_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocation {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="store_location_id")
    private Integer id;

    // ==================================
    // === THÊM TRƯỜNG NÀY VÀO ===
    @Column(name="store_name", nullable=false, length=150)
    private String storeName;
    // ==================================

    @Column(precision=9, scale=6, nullable=false)
    private BigDecimal latitude;

    @Column(precision=9, scale=6, nullable=false)
    private BigDecimal longitude;

    @Column(nullable=false, length=255)
    private String address; // Đây sẽ là địa chỉ đầy đủ (ví dụ: Chợ Bến Thành...)

    @Builder.Default
    @Column(name = "is_hub", nullable = false)
    private boolean hub = false;

}
