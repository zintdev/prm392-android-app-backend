package com.example.backend.dto.store;

import java.math.BigDecimal;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocationResponse {
    private Integer id;
    
    // ==================================
    // === THÊM TRƯỜNG NÀY VÀO ===
    private String storeName;
    // ==================================

    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
}
