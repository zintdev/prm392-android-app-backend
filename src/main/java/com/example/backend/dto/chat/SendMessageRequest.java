package com.example.backend.dto.chat;


import com.example.backend.domain.entity.Message;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessageRequest {
    @NotNull
    private Integer receiverId; // <-- THAY ĐỔI: Long -> Integer
    @NotNull
    private Message.MessageType messageType;
    @NotEmpty
    private String content;
    // Getters and setters
    public Integer getReceiverId() { return receiverId; }
    // ...
}