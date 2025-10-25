package com.example.backend.dto.order;

import com.example.backend.domain.enums.ShipmentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "CreateOrderRequest", description = "Create order request body")
public class CreateOrderRequest {
    @NotNull
    private Integer userId;
    
    @NotNull
    private ShipmentMethod shipmentMethod;
    
    @NotNull
    private String shippingFullName;
    
    @NotNull
    private String shippingPhone;
    
    @NotNull
    private String shippingAddressLine1;
    
    private String shippingAddressLine2;
    
    @NotNull
    private String shippingCityState;
}
