package com.example.backend.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "conversation_participants")
public class ConversationParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // <-- THAY ĐỔI: Long -> Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(name = "user_id", nullable = false)
    private Integer userId; // <-- THAY ĐỔI: Long -> Integer (Khớp với users.user_id)

    @Column(columnDefinition = "TIMESTAMPTZ not null default now()")
    private Instant joinedAt = Instant.now();
    
}