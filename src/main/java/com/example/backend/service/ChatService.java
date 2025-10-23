package com.example.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Conversation;
import com.example.backend.domain.entity.ConversationParticipant;
import com.example.backend.domain.entity.Message;
import com.example.backend.dto.chat.MessageDto;
import com.example.backend.dto.chat.ReadReceiptRequest;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.dto.chat.TypingEventRequest;
import com.example.backend.repository.ConversationParticipantRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.MessageRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.*;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {
    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final FirebaseChatService firebaseService;
    private final ConversationParticipantRepository participantRepo;


    public MessageDto sendMessage(Integer senderId, SendMessageRequest request) { // <-- THAY ĐỔI



        Conversation conversation = findOrCreateConversation(senderId, request.getReceiverId());

        Message message = new Message(
            conversation,
            senderId,
            request.getMessageType(),
            request.getContent()
        );
        messageRepo.save(message);

        conversation.setLastMessageAt(Instant.now());
        conversationRepo.save(conversation);

        MessageDto messageDto = MessageDto.fromEntity(message);
        firebaseService.pushNewMessage(messageDto);
        return messageDto;
    }

    private Conversation findOrCreateConversation(Integer senderId, Integer receiverId) { // <-- THAY ĐỔI
        List<Integer> convIds = conversationRepo.findConversationIdsBetweenUsers(senderId, receiverId); // <-- THAY ĐỔI
        
        if (!convIds.isEmpty()) {
            return conversationRepo.findById(convIds.get(0)).orElseThrow();
        }

        // Tạo mới
        Conversation conversation = new Conversation();
        conversation = conversationRepo.save(conversation);

        ConversationParticipant senderPart = new ConversationParticipant();
        senderPart.setConversation(conversation);
        senderPart.setUserId(senderId);
        participantRepo.save(senderPart);

        ConversationParticipant receiverPart = new ConversationParticipant();
        receiverPart.setConversation(conversation);
        receiverPart.setUserId(receiverId);
        participantRepo.save(receiverPart);

        return conversation;
    }

    public List<MessageDto> getMessageHistory(Integer conversationId) { // <-- THAY ĐỔI
        // TODO: Check quyền
        return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(MessageDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void handleTypingEvent(Integer senderId, TypingEventRequest request) { // <-- THAY ĐỔI
        firebaseService.pushTypingEvent(request.getConversationId(), senderId, request.getIsTyping());
    }

    @Transactional
    public void handleReadReceipt(Integer senderId, ReadReceiptRequest request) { // <-- THAY ĐỔI
        Message message = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
        if (message.getReadAt() == null) {
            message.setReadAt(Instant.now());
            messageRepo.save(message);

            MessageDto messageDto = MessageDto.fromEntity(message);
            firebaseService.pushReadReceipt(message.getConversation().getId(), messageDto);
        }
    }
}