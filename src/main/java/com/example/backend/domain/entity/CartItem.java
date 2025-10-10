package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

@Entity
@Table(name = "cart_items",
       uniqueConstraints = @UniqueConstraint(name = "uk_cart_product", columnNames = {"cart_id","product_id"}),
       indexes = @Index(name = "idx_cart_items_cart", columnList = "cart_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "cart_item_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "cart_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_cart_item_cart"))
  private Cart cart;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_cart_item_product"))
  private Product product;

  @Min(1)
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "is_selected", nullable = false)
  private Boolean selected = Boolean.TRUE;
}
