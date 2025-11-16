package com.example.backend.dto.chat;

// <-- SỬA LỖI: Xóa import sai của Firebase
import jakarta.validation.constraints.NotNull; // <-- SỬA LỖI: Import đúng của Jakarta Validation
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TypingEventRequest {
    @NotNull // <-- Annotation này giờ đã đúng
    private Integer conversationId;
    @NotNull // <-- Annotation này giờ đã đúng
    private Boolean isTyping;
    
    public Integer getConversationId() { return conversationId; }
    public Boolean getIsTyping() { return isTyping; }
}