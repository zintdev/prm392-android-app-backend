package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcType;
import org.hibernate.dialect.PostgreSQLEnumJdbcType;

import com.example.backend.domain.enums.UserRole;

@Entity
@Table(
    name = "users",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_users_username", columnNames = "username"),
        @UniqueConstraint(name = "uq_users_email", columnNames = "email")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // INT GENERATED ALWAYS AS IDENTITY
    private Integer id;

    @NotBlank
    @Size(max = 150)
    @Column(name = "username", nullable = false, length = 150)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(name = "email", nullable = false, length = 255)
    private String email;

    @NotBlank
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    // PostgreSQL enum user_role mapped as String; columnDefinition giữ đúng kiểu bên DB
    @Enumerated(EnumType.STRING)
    @JdbcType(PostgreSQLEnumJdbcType.class)                 // ✅ báo cho Hibernate đây là Postgres ENUM
    @Column(name = "role", nullable = false, columnDefinition = "user_role")  // ✅ đúng tên type ở DB
    private UserRole role = UserRole.CUSTOMER;

    // DB tự set DEFAULT now(); để insertable=false, updatable=false dùng giá trị từ DB
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;
}
