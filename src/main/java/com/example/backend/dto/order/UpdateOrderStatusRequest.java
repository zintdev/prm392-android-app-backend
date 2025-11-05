package com.example.backend.dto.order;

import com.example.backend.domain.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UpdateOrderStatusRequest", description = "Update order status request body")
public class UpdateOrderStatusRequest {
    @NotNull
    private OrderStatus orderStatus;
}
