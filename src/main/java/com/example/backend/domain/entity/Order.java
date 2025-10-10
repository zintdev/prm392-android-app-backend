package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="order_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="cart_id")
    private Cart cart; // nullable

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name="order_status", columnDefinition="order_status", nullable=false)
    private com.example.backend.domain.enums.OrderStatus orderStatus;

    @Column(name="order_date", nullable=false)
    private OffsetDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(name="shipment_method", columnDefinition="shipment_method", nullable=false)
    private com.example.backend.domain.enums.ShipmentMethod shipmentMethod;

    @Column(name="shipping_full_name", length=150)
    private String shippingFullName;

    @Column(name="shipping_phone", length=20)
    private String shippingPhone;

    @Column(name="shipping_address_line1", length=255)
    private String shippingAddressLine1;

    @Column(name="shipping_address_line2", length=255)
    private String shippingAddressLine2;

    @Column(name="shipping_city_state", length=255)
    private String shippingCityState;

    @OneToOne(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    private Payment payment;

    @OneToMany(mappedBy="order", cascade=CascadeType.ALL, orphanRemoval=true)
    private java.util.List<OrderItem> items = new java.util.ArrayList<>();

    @PrePersist
    public void prePersist() {{
        if (orderDate == null) orderDate = OffsetDateTime.now();
        if (orderStatus == null) orderStatus = com.example.backend.domain.enums.OrderStatus.PENDING;
        if (shipmentMethod == null) shipmentMethod = com.example.backend.domain.enums.ShipmentMethod.DELIVERY;
    }}

}
