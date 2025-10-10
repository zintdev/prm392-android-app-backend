package com.example.backend.domain.entity;

import com.example.backend.domain.enums.OrderStatus;
import com.example.backend.domain.enums.ShipmentMethod;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "orders",
       indexes = @Index(name = "idx_orders_user_date", columnList = "user_id, order_date DESC"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_order_user"))
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id",
              foreignKey = @ForeignKey(name = "fk_order_cart"))
  private Cart cart;

  @Enumerated(EnumType.STRING)
  @Column(name = "order_status", nullable = false)
  private OrderStatus orderStatus = OrderStatus.PENDING;

  @CreationTimestamp
  @Column(name = "order_date", nullable = false)
  private OffsetDateTime orderDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "shipment_method", nullable = false)
  private ShipmentMethod shipmentMethod = ShipmentMethod.DELIVERY;

  @Column(name = "shipping_full_name", length = 150)
  private String shippingFullName;

  @Column(name = "shipping_phone", length = 20)
  private String shippingPhone;

  @Column(name = "shipping_address_line1", length = 255)
  private String shippingAddressLine1;

  @Column(name = "shipping_address_line2", length = 255)
  private String shippingAddressLine2;

  @Column(name = "shipping_city_state", length = 255)
  private String shippingCityState;
}
