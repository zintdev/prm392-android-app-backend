package com.example.backend.domain.entity;

import com.example.backend.domain.enums.CartStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "carts", indexes = @Index(name = "idx_cart_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_cart_user"))
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private CartStatus status = CartStatus.ACTIVE;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
