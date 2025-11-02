package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "AddressResponse", description = "Address response body")
public class AddressResponse {
    private Integer id;
    private Integer userId;

    // === BỔ SUNG ===
    @Schema(description = "Full name of the recipient", example = "Nguyen Van A")
    private String fullName;

    @Schema(description = "Phone number of the recipient", example = "0901234567")
    private String phoneNumber;
    // ===============

    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;

    // === BỔ SUNG ===
    @Schema(description = "Is this the default address for the user", example = "true")
    private Boolean isDefault;
    // ===============

    private OffsetDateTime createdAt;
}