package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "UpdateAddressRequest", description = "Update address request body")
public class UpdateAddressRequest {
    private Integer userId;

    // === BỔ SUNG ===
    @Schema(description = "Full name of the recipient", example = "Nguyen Van B")
    private String fullName;

    @Schema(description = "Phone number of the recipient", example = "090111222")
    private String phoneNumber;
    // ===============

    @Schema(description = "Address line 1", example = "456 Le Loi")
    private String shippingAddressLine1;

    @Schema(description = "Address line 2 (optional)", example = "Floor 5")
    private String shippingAddressLine2;

    @Schema(description = "City and State/Province", example = "District 3, Ho Chi Minh City")
    private String shippingCityState;

    // === BỔ SUNG ===
    @Schema(description = "Set as default address", example = "true")
    private Boolean isDefault;
    // ===============
}