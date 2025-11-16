package com.example.backend.dto.chat;

import java.util.List;
import java.util.stream.Collectors;

import com.example.backend.domain.entity.Conversation;
import com.example.backend.domain.entity.ConversationParticipant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO này dùng để trả về thông tin cuộc hội thoại (chủ yếu là ID)
 * khi Android gọi API "find-or-create".
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {

    private Integer conversationId;
    private List<Integer> participantIds;

    /**
     * Hàm Factory để chuyển từ Entity -> DTO
     */
    public static ConversationDto fromEntity(Conversation conversation) {
        List<Integer> participantIds = conversation.getParticipants()
                .stream()
                .map(ConversationParticipant::getUserId)
                .collect(Collectors.toList());
        
        return ConversationDto.builder()
                .conversationId(conversation.getId())
                .participantIds(participantIds)
                .build();
    }
}