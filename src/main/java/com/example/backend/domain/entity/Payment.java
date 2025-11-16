package com.example.backend.domain.entity;
 
import com.example.backend.domain.enums.PaymentMethod;
import com.example.backend.domain.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Payment {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="payment_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="order_id", unique=true)
    private Order order;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition="payment_method", nullable=false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition="payment_status", nullable=false)
    private PaymentStatus status;

    @Column(nullable=false, precision=12, scale=2)
    private BigDecimal amount;

    @Column(name="paid_at")
    private OffsetDateTime paidAt;

    @PrePersist
    public void prePersist() {{
        if (status == null) status = com.example.backend.domain.enums.PaymentStatus.PENDING;
    }}

}
