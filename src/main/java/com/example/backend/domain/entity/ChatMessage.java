package com.example.backend.domain.entity;
 
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "chat_messages")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="chat_message_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id")
    private User user;

    @Column(columnDefinition="TEXT", nullable=false)
    private String message;

    @Column(name="sent_at", nullable=false)
    private OffsetDateTime sentAt;

    @PrePersist
    public void prePersist() {{
        if (sentAt == null) sentAt = OffsetDateTime.now();
    }}

}
