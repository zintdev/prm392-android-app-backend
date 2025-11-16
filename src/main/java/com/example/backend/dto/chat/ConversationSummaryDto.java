package com.example.backend.dto.chat;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ConversationSummaryDto {
    private Integer conversationId;
    private List<Integer> participantIds;
    private MessageDto lastMessage;
    private Long lastMessageAt;
    
    // Additional fields for admin UI
    private Integer customerId;
    private String customerName;
    private String customerEmail;
    private Integer unreadCount;
}
