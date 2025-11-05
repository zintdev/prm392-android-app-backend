package com.example.backend.dto.order;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "OrderItemResponse", description = "Order item response body")
public class OrderItemResponse {
    private Integer id;
    private Integer productId;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal taxRate;
    private String currencyCode;
    private String imageUrl;
}
