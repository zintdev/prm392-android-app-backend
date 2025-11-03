package com.example.backend.dto.store;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocationRequest {
    @NotBlank @Size(max=150)
    private String storeName;

    @NotNull @DecimalMin(value="-90.0")  @DecimalMax(value="90.0")
    private BigDecimal latitude;

    @NotNull @DecimalMin(value="-180.0") @DecimalMax(value="180.0")
    private BigDecimal longitude;

    @NotBlank @Size(max=255)
    private String address;
}