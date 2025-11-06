package com.example.backend.dto.store;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInventoryUpdateRequest {

    @NotNull
    @Min(1)
    private Integer productId;

    @NotNull
    @Min(0)
    private Integer quantity;

    @NotNull
    @Min(1)
    private Integer actorUserId;
}
