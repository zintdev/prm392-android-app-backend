package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // === BỔ SUNG ===
    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;
    // ===============

    @Column(name="shipping_address_line1", nullable = false) // Thêm nullable
    private String shippingAddressLine1;

    @Column(name="shipping_address_line2")
    private String shippingAddressLine2;

    @Column(name="shipping_city_state", nullable = false) // Thêm nullable
    private String shippingCityState;

    // === BỔ SUNG ===
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;
    // ===============

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.OffsetDateTime createdAt;
}