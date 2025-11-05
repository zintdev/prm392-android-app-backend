package com.example.backend.dto.order;

import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.domain.enums.ShipmentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "OrderResponse", description = "Order response body")
public class OrderResponse {
    private Integer id;
    private Integer userId;
    private Integer cartId;
    private OrderStatus orderStatus;
    private ShipmentMethod shipmentMethod;
    private OffsetDateTime orderDate;
    private String shippingFullName;
    private String shippingPhone;
    private String shippingAddressLine1;
    private String shippingAddressLine2;
    private String shippingCityState;
    private BigDecimal subtotal;
    private BigDecimal taxTotal;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
    private List<OrderItemResponse> items;
}
