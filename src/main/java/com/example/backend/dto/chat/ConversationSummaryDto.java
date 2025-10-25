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
    private MessageDto lastMessage; // (File này giờ đã chứa Long)
    
    // THAY ĐỔI: Instant -> Long
    private Long lastMessageAt;
}
