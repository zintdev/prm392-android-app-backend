package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.*;
import com.example.backend.domain.enums.*;

@Entity @Table(name="orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="order_id") private Integer id;

  @ManyToOne(fetch=FetchType.LAZY) 
  @JoinColumn(name="user_id", nullable=false)
  private User user;

  @Enumerated(EnumType.STRING) 
  @Column(name="order_status", nullable=false)
  private OrderStatus orderStatus;

  @Enumerated(EnumType.STRING) 
  @Column(name="shipment_method", nullable=false)
  private ShipmentMethod shipmentMethod;

  @Column(name="order_date", nullable=false) 
  private OffsetDateTime orderDate;

  private String shippingFullName; 
  private String shippingPhone;
  @Column(name="shipping_address_line1") private String shippingAddressLine1;
  @Column(name="shipping_address_line2") private String shippingAddressLine2;
  @Column(name="shipping_city_state")    private String shippingCityState;

  // totals
  @Column(name="subtotal",     nullable=false, precision=12, scale=2)
  private BigDecimal subtotal = BigDecimal.ZERO;
  @Column(name="tax_total",    nullable=false, precision=12, scale=2)
  private BigDecimal taxTotal = BigDecimal.ZERO;
  @Column(name="shipping_fee", nullable=false, precision=12, scale=2)
  private BigDecimal shippingFee = BigDecimal.ZERO;
  @Column(name="grand_total",  nullable=false, precision=12, scale=2)
  private BigDecimal grandTotal = BigDecimal.ZERO;

  @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
  private List<OrderItem> items = new ArrayList<>();

  @PrePersist void pre() { if (orderDate==null) orderDate = OffsetDateTime.now(); }
}