package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "CreateAddressRequest", description = "Create address request body")
public class CreateAddressRequest {
    @NotNull
    private Integer userId;

    @NotNull
    private String fullName;

    private String phoneNumber;

    @NotNull
    private String shippingAddressLine1;
    
    private String shippingAddressLine2;
    
    @NotNull
    private String shippingCityState;
}