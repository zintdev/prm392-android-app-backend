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
    private Integer id; // <-- THAY ĐỔI
    private Integer conversationId; // <-- THAY ĐỔI
    private Integer senderId; // <-- THAY ĐỔI
    private Message.MessageType messageType;
    private String content;
    private Instant createdAt;
    private Instant readAt;

    public static MessageDto fromEntity(Message message) {
        MessageDto dto = new MessageDto();
        dto.setId(message.getId());
        dto.setConversationId(message.getConversation().getId());
        dto.setSenderId(message.getSenderId());
        dto.setMessageType(message.getMessageType());
        dto.setContent(message.getContent());
        dto.setCreatedAt(message.getCreatedAt());
        dto.setReadAt(message.getReadAt());
        return dto;
    }
}