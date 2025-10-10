package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "products",
       indexes = {
         @Index(name = "idx_products_category", columnList = "category_id"),
         @Index(name = "idx_products_publisher", columnList = "publisher_id"),
         @Index(name = "idx_products_artist", columnList = "artist_id")
       })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "product_id")
  private Integer id;

  @NotBlank
  @Column(name = "product_name", length = 255, nullable = false)
  private String productName;

  @Column(name = "description")
  private String description;

  @Column(name = "price", precision = 12, scale = 2, nullable = false)
  private BigDecimal price;

  @PositiveOrZero
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "artist_id", foreignKey = @ForeignKey(name = "fk_product_artist"))
  private Artist artist;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "publisher_id", foreignKey = @ForeignKey(name = "fk_product_publisher"))
  private Publisher publisher;

  @Column(name = "release_date")
  private LocalDate releaseDate;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", foreignKey = @ForeignKey(name = "fk_product_category"))
  private Category category;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
