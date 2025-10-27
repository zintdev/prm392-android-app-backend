package com.example.backend.dto.chat;

import java.time.Instant;
import com.example.backend.domain.entity.Message;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class MessageDto {
    private Integer id;
    private Integer conversationId;
    private Integer senderId;
    private Message.MessageType messageType;
    private String content;

    // THAY ĐỔI: Instant -> Long
    private Long createdAt;
    // Removed readAt - using message_reads table instead

    public static MessageDto fromEntity(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSenderId());
        dto.setMessageType(message.getMessageType());
        dto.setContent(message.getContent());

        // THAY ĐỔI: Chuyển Instant sang Long (miligiây)
        if (message.getCreatedAt() != null) {
            dto.setCreatedAt(message.getCreatedAt().toEpochMilli());
        }
        // readAt removed - using message_reads table
        
        return dto;
    }
}