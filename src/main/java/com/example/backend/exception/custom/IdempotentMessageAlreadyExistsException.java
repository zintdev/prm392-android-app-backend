package com.example.backend.exception.custom;

import com.example.backend.dto.chat.MessageDto;

public class IdempotentMessageAlreadyExistsException extends RuntimeException {
    private final transient MessageDto messageDto;
    public IdempotentMessageAlreadyExistsException(MessageDto messageDto) {
        super("Message already exists (idempotent)");
        this.messageDto = messageDto;
    }
    public MessageDto getMessageDto() { return messageDto; }
}
