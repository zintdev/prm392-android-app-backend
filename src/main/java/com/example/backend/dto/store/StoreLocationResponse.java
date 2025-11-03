package com.example.backend.dto.store;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.*;


@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocationResponse {
    private Integer id;
    private String storeName;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
}
