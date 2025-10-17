package com.example.backend.dto.cart;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data public class CartItemRequest {
  @NotNull private Integer productId;
  @Min(1)  private int quantity;
}

