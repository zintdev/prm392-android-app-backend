package com.example.backend.dto.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventoryResponse {
    private Integer storeLocationId;
    private String storeName;
    private Integer productId;
    private String productName;
    private String productImageUrl;
    private Integer quantity;
    private Integer productQuantity;
    private Integer totalAllocatedQuantity;
    private Integer remainingQuantity;
    private Integer availableForStore;
}
