package com.example.backend.dto.cart;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder public class CartResponse {
  private Integer cartId;
  private List<Item> items;
  private BigDecimal subtotal, taxTotal, shippingFee, grandTotal;

  @Data @Builder public static class Item {
    private Integer cartItemId, productId; private String productName;
    private String imageUrl;
    private BigDecimal unitPrice; private int quantity; private boolean selected;
  }
}
