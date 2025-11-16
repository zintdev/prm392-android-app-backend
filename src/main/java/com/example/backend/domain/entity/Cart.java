package com.example.backend.domain.entity;

import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.example.backend.domain.enums.*;
import java.util.*;

@Entity
@Table(name = "carts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, columnDefinition = "cart_status")
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private CartStatus status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;

  // totals (NOT NULL) -> bắt buộc dùng @Builder.Default
  @Builder.Default
  @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal taxTotal = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
  private BigDecimal shippingFee = BigDecimal.ZERO;

  @Builder.Default
  @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
  private BigDecimal grandTotal = BigDecimal.ZERO;

  @Builder.Default
  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<CartItem> items = new ArrayList<>();

  @PrePersist
  void pre() {
    if (createdAt == null) createdAt = OffsetDateTime.now();
    if (status == null) status = CartStatus.ACTIVE;
  }
}
