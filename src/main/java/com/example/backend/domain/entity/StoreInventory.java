package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "store_inventory", uniqueConstraints = @UniqueConstraint(columnNames = {"store_location_id","product_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreInventory {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="store_inventory_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="store_location_id")
    private StoreLocation storeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="product_id")
    private Product product;

    @Column(nullable=false)
    private Integer quantity;

}
