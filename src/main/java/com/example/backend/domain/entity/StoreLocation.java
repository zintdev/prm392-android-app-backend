package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "store_locations")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class StoreLocation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "store_location_id")
  private Integer id;

  @Column(name = "latitude", precision = 9, scale = 6, nullable = false)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 9, scale = 6, nullable = false)
  private BigDecimal longitude;

  @NotBlank
  @Column(name = "address", length = 255, nullable = false)
  private String address;
}
