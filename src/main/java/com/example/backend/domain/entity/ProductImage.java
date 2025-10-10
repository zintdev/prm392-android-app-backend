package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_images")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductImage {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="image_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="product_id")
    private Product product;

    @Column(columnDefinition="TEXT", nullable=false)
    private String url;

    @Column(name="sort_order", nullable=false)
    private Integer sortOrder = 0;

}
