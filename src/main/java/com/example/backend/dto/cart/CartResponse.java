package com.example.backend.dto.cart;

import java.math.BigDecimal;
import java.util.List;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    private Integer cartId;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
    private List<Item> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {         
        private Integer cartItemId;
        private Integer productId;
        private String  productName;
        private String  imageUrl;
        private BigDecimal unitPrice;
        private Integer quantity;
        private boolean selected;
        private String  currency;      
        private BigDecimal taxRate;    
    }
}
