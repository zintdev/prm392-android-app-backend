package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import com.example.backend.domain.enums.*;

@Entity @Table(name="orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="order_id") private Integer id;

  @ManyToOne(fetch=FetchType.LAZY) 
  @JoinColumn(name="user_id", nullable=false)
  private User user;

  @ManyToOne(fetch=FetchType.LAZY) 
  @JoinColumn(name="cart_id")
  private Cart cart;

  @Enumerated(EnumType.STRING)
  @Column(name="order_status", nullable=false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private OrderStatus orderStatus;

  @Enumerated(EnumType.STRING) 
  @Column(name="shipment_method", nullable=false)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private ShipmentMethod shipmentMethod;

  @Column(name="order_date", nullable=false) 
  private OffsetDateTime orderDate;

  private String shippingFullName; 
  private String shippingPhone;
  @Column(name="shipping_address_line1") private String shippingAddressLine1;
  @Column(name="shipping_address_line2") private String shippingAddressLine2;
  @Column(name="shipping_city_state")    private String shippingCityState;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name="store_location_id")
  private StoreLocation storeLocation;

  @Column(name="keeping_expires_at")
  private OffsetDateTime keepingExpiresAt;

  // totals
  @Builder.Default
  @Column(name="subtotal",     nullable=false, precision=12, scale=2)
  private BigDecimal subtotal = BigDecimal.ZERO;
  @Builder.Default
  @Column(name="tax_total",    nullable=false, precision=12, scale=2)
  private BigDecimal taxTotal = BigDecimal.ZERO;
  @Builder.Default
  @Column(name="shipping_fee", nullable=false, precision=12, scale=2)
  private BigDecimal shippingFee = BigDecimal.ZERO;
  @Builder.Default
  @Column(name="grand_total",  nullable=false, precision=12, scale=2)
  private BigDecimal grandTotal = BigDecimal.ZERO;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<OrderItem> items = new ArrayList<>();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<OrderFulfillmentSource> fulfillmentSources = new ArrayList<>();


  @PrePersist void pre() { if (orderDate==null) orderDate = OffsetDateTime.now(); }
}