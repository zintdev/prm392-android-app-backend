package com.example.backend.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "chat_messages", indexes = @Index(name = "idx_chat_user", columnList = "user_id"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatMessage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "chat_message_id")
  private Integer id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false,
              foreignKey = @ForeignKey(name = "fk_chat_user"))
  private User user;

  @NotBlank
  @Column(name = "message", nullable = false)
  private String message;

  @CreationTimestamp
  @Column(name = "sent_at", nullable = false)
  private OffsetDateTime sentAt;
}
