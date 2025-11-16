package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="notification_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @Column(columnDefinition="TEXT", nullable=false)
    private String message;

    @Column(name="is_read", nullable=false)
    private Boolean read;

    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {{
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (read == null) read = Boolean.FALSE;
    }}

}
