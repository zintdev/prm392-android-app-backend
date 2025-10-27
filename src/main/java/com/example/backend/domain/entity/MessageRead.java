package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "message_reads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@IdClass(MessageReadId.class)
public class MessageRead {
    
    @Id
    @Column(name = "message_id")
    private Integer messageId;
    
    @Id
    @Column(name = "user_id")
    private Integer userId;
    
    @Column(name = "read_at", nullable = false)
    @Builder.Default
    private Instant readAt = Instant.now();
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    private Message message;
    
    @PrePersist
    public void prePersist() {
        if (this.readAt == null) {
            this.readAt = Instant.now();
        }
    }
}
