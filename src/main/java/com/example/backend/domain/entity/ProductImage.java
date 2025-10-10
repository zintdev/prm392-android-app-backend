package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "image_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "product_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_product_image_product"))
  private Product product;

  @NotBlank
  @Column(name = "url", nullable = false)
  private String url;

  @Column(name = "sort_order", nullable = false)
  private Integer sortOrder = 0;
}
