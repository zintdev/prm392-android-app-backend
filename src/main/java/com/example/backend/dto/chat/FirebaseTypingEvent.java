package com.example.backend.dto.chat;

import lombok.Getter;

@Getter
public class FirebaseTypingEvent {
    private Integer userId; // <-- THAY ĐỔI
    private Boolean isTyping;
    public FirebaseTypingEvent(Integer userId, Boolean isTyping) { // <-- THAY ĐỔI
        this.userId = userId;
        this.isTyping = isTyping;
    }
}