package com.example.backend.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "conversations")
public class Conversation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // <-- THAY ĐỔI: Long -> Integer

    @Column(columnDefinition = "TIMESTAMPTZ not null default now()")
    private Instant createdAt = Instant.now();

    @Column(columnDefinition = "TIMESTAMPTZ default now()")
    private Instant lastMessageAt = Instant.now();

    @OneToMany(mappedBy = "conversation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<ConversationParticipant> participants = new HashSet<>();
    
    public Integer getId() { return id; }
}