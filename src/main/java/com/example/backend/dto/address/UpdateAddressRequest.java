package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UpdateAddressRequest", description = "Update address request body")
public class UpdateAddressRequest {
    private Integer userId;
    private String fullName;
    private String phoneNumber;
    private String shippingAddressLine1;

    @Schema(description = "Address line 2 (optional)", example = "Floor 5")
    private String shippingAddressLine2;

    @Schema(description = "City and State/Province", example = "District 3, Ho Chi Minh City")
    private String shippingCityState;
    private Boolean isDefault;
}