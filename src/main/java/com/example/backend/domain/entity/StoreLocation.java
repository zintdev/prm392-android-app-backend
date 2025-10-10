package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "store_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocation {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="store_location_id")
    private Integer id;

    @Column(precision=9, scale=6, nullable=false)
    private BigDecimal latitude;

    @Column(precision=9, scale=6, nullable=false)
    private BigDecimal longitude;

    @Column(nullable=false, length=255)
    private String address;

}
