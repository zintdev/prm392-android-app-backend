package com.example.backend.dto.address;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.time.OffsetDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "AddressResponse", description = "Address response body")
public class AddressResponse {
    private Integer id;
    private Integer userId;
    private String fullName;
    private String phoneNumber;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;
    private Boolean isDefault;
    private OffsetDateTime createdAt;
}