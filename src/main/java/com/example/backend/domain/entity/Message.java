package com.example.backend.domain.entity;

import jakarta.persistence.*;

import java.time.Instant;
import lombok.*;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "messages")
public class Message {

    public enum MessageType {
        TEXT, IMAGE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // <-- THAY ĐỔI: Long -> Integer

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @Column(nullable = false)
    private Integer senderId; // <-- THAY ĐỔI: Long -> Integer (Khớp với users.user_id)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType messageType = MessageType.TEXT;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TIMESTAMPTZ not null default now()")
    private Instant createdAt = Instant.now();

    @Column(columnDefinition = "TIMESTAMPTZ")
    private Instant readAt;
    
    // Getters and setters...
    
    public Message(Conversation conversation, Integer senderId, MessageType messageType, String content) {
        this.conversation = conversation;
        this.senderId = senderId;
        this.messageType = messageType;
        this.content = content;
    }
}