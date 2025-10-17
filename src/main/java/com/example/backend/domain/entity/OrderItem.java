package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name="order_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class OrderItem {
  @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
  @Column(name="order_item_id") private Integer id;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="order_id", nullable=false)
  private Order order;

  @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="product_id")
  private Product product; // có thể null nếu sản phẩm bị xoá

  @Column(name="product_name", nullable=false) private String productName;

  @Column(name="unit_price", nullable=false, precision=12, scale=2)
  private BigDecimal unitPrice;

  @Column(name="quantity", nullable=false) private Integer quantity;

  // NEW
  @Column(name="tax_rate", nullable=false, precision=5, scale=2)
  private BigDecimal taxRate = BigDecimal.ZERO;

@Column(name="currency_code", nullable=false, length=3, columnDefinition = "char(3)")
private String currencyCode = "VND";
}
