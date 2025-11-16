package com.example.backend.dto.order;

import com.example.backend.domain.enums.ShipmentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data public class CheckoutRequest {
  @NotBlank private String shippingFullName;
  @NotBlank private String shippingPhone;
  @NotBlank private String address1;
  private String address2;
  @NotBlank private String cityState;
  @NotNull  private ShipmentMethod shipmentMethod; // DELIVERY|PICKUP
}
