package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="cart_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CartItem {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="cart_item_id") private Integer id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="cart_id", nullable=false)
  private Cart cart;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id", nullable=false)
  private Product product;

  @Column(name="quantity", nullable=false) private Integer quantity;

  @Column(name="is_selected", nullable=false) private boolean selected = true;

  // NEW snapshot
  @Column(name="unit_price", nullable=false, precision=12, scale=2)
  private BigDecimal unitPrice;

  @Column(name="tax_rate", nullable=false, precision=5, scale=2)
  private BigDecimal taxRate = BigDecimal.ZERO;

@Column(name="currency_code", nullable=false, length=3, columnDefinition = "char(3)")
private String currencyCode = "VND";
}

