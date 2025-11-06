package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(nullable=false, unique=true, length=150)
    private String username;

    @Column(nullable=false, unique=true, length=255)
    private String email;

    @Column(name="password_hash", nullable=false, length=255)
    private String passwordHash;

    @Column(name="phone_number", length=20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "user_role", nullable=false)
    private com.example.backend.domain.enums.UserRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_location_id")
    private StoreLocation storeLocation;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (role == null) role = com.example.backend.domain.enums.UserRole.CUSTOMER;
    }

}
