package com.example.backend.domain.entity;

import com.example.backend.domain.enums.PaymentMethod;
import com.example.backend.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments",
       indexes = @Index(name = "idx_payments_status", columnList = "status"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "payment_id")
  private Integer id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false, unique = true,
              foreignKey = @ForeignKey(name = "fk_payment_order"))
  private Order order;

  @Enumerated(EnumType.STRING)
  @Column(name = "method", nullable = false)
  private PaymentMethod method;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "amount", precision = 12, scale = 2, nullable = false)
  private BigDecimal amount;

  @Column(name = "paid_at")
  private OffsetDateTime paidAt;
}
