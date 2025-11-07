package com.example.backend.domain.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "order_fulfillment_sources",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_item_id", "store_location_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFulfillmentSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_fulfillment_source_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private OrderItem orderItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "store_location_id", nullable = false)
    private StoreLocation storeLocation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "soft_reserved_quantity", nullable = false)
    private Integer softReservedQuantity;

    @Builder.Default
    @Column(name = "hard_deducted_quantity", nullable = false)
    private Integer hardDeductedQuantity = 0;

    @Column(name = "soft_reserved_at", nullable = false)
    private OffsetDateTime softReservedAt;

    @Column(name = "hard_deducted_at")
    private OffsetDateTime hardDeductedAt;

    @PrePersist
    void ensureDefaults() {
        if (softReservedAt == null) {
            softReservedAt = OffsetDateTime.now();
        }
        if (softReservedQuantity == null) {
            softReservedQuantity = 0;
        }
        if (hardDeductedQuantity == null) {
            hardDeductedQuantity = 0;
        }
    }
}
