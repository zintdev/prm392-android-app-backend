package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "CreateAddressRequest", description = "Create address request body")
public class CreateAddressRequest {

    @NotNull
    @Schema(description = "User ID", example = "1")
    private Integer userId;

    // === BỔ SUNG ===
    @NotBlank
    @Size(max = 150)
    @Schema(description = "Full name of the recipient", example = "Nguyen Van A")
    private String fullName;

    @NotBlank
    @Size(max = 20)
    @Schema(description = "Phone number of the recipient", example = "0901234567")
    private String phoneNumber;
    // ===============
    
    @NotBlank
    @Schema(description = "Address line 1", example = "123 Vo Van Kiet")
    private String shippingAddressLine1;
    
    @Schema(description = "Address line 2 (optional)", example = "Apartment 4B")
    private String shippingAddressLine2;
    
    @NotBlank
    @Schema(description = "City and State/Province", example = "District 1, Ho Chi Minh City")
    private String shippingCityState;

    // === BỔ SUNG ===
    @Schema(description = "Set as default address (optional, defaults to false)", example = "true")
    private Boolean isDefault;
    // ===============
}