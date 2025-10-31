package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UpdateAddressRequest", description = "Update address request body")
public class UpdateAddressRequest {
    private Integer userId;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;
}