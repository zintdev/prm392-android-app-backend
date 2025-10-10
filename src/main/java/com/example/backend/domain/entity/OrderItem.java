package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_item_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "order_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_order_item_order"))
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id",
              foreignKey = @ForeignKey(name = "fk_order_item_product"))
  private Product product; // có thể null nếu SP bị xóa; giữ lại tên/giá snapshot

  @Column(name = "product_name", length = 255, nullable = false)
  private String productName;

  @Column(name = "unit_price", precision = 12, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @Min(1)
  @Column(name = "quantity", nullable = false)
  private Integer quantity;
}
