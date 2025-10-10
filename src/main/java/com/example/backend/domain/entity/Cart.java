package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "carts")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Cart {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="cart_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.OTHER)
    @Column(columnDefinition="cart_status", nullable=false)
    private com.example.backend.domain.enums.CartStatus status;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @OneToMany(mappedBy="cart", cascade=CascadeType.ALL, orphanRemoval=true)
    private java.util.List<CartItem> items = new java.util.ArrayList<>();

    @PrePersist
    public void prePersist() {{
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (status == null) status = com.example.backend.domain.enums.CartStatus.ACTIVE;
    }}

}
