// package com.example.backend.service;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;

// import com.example.backend.domain.entity.Conversation;
// import com.example.backend.domain.entity.ConversationParticipant;
// import com.example.backend.domain.entity.Message;
// import com.example.backend.dto.chat.MessageDto;
// import com.example.backend.dto.chat.ReadReceiptRequest;
// import com.example.backend.dto.chat.SendMessageRequest;
// import com.example.backend.dto.chat.TypingEventRequest;
// import com.example.backend.repository.ConversationParticipantRepository;
// import com.example.backend.repository.ConversationRepository;
// import com.example.backend.repository.MessageRepository;

// import jakarta.persistence.EntityNotFoundException;
// import jakarta.validation.*;
// import lombok.RequiredArgsConstructor;

// import java.time.Instant;
// import java.util.List;
// import java.util.stream.Collectors;


// @Service
// @RequiredArgsConstructor
// @Transactional
// public class ChatService {
//     private final MessageRepository messageRepo;
//     private final ConversationRepository conversationRepo;
//     private final FirebaseChatService firebaseService;
//     private final ConversationParticipantRepository participantRepo;
//     private final UserService userService;


//     public MessageDto sendMessage(Integer senderId, SendMessageRequest request) { // <-- THAY ĐỔI



//         Conversation conversation = findOrCreateConversation(senderId, request.getReceiverId());

//         Message message = new Message(
//             conversation,
//             senderId,
//             request.getMessageType(),
//             request.getContent()
//         );
//         messageRepo.save(message);

//         conversation.setLastMessageAt(Instant.now());
//         conversationRepo.save(conversation);

//         MessageDto messageDto = MessageDto.fromEntity(message);
//         firebaseService.pushNewMessage(messageDto);
//         return messageDto;
//     }

//     private Conversation findOrCreateConversation(Integer senderId, Integer receiverId) { // <-- THAY ĐỔI
//         List<Integer> convIds = conversationRepo.findConversationIdsBetweenUsers(senderId, receiverId); // <-- THAY ĐỔI
        
//         if (!convIds.isEmpty()) {
//             return conversationRepo.findById(convIds.get(0)).orElseThrow();
//         }

//         // Tạo mới
//         Conversation conversation = new Conversation();
//         conversation = conversationRepo.save(conversation);

//         ConversationParticipant senderPart = new ConversationParticipant();
//         senderPart.setConversation(conversation);
//         senderPart.setUserId(senderId);
//         participantRepo.save(senderPart);

//         ConversationParticipant receiverPart = new ConversationParticipant();
//         receiverPart.setConversation(conversation);
//         receiverPart.setUserId(receiverId);
//         participantRepo.save(receiverPart);

//         return conversation;
//     }

// public List<MessageDto> getMessageHistory(Integer currentUserId, Integer conversationId) {
//         Conversation conv = conversationRepo.findById(conversationId)
//                 .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

//         boolean isAdmin = userService.isAdmin(currentUserId);
//         boolean isParticipant = participantRepo.existsByConversationIdAndUserId(conversationId, currentUserId);

//         if (!isAdmin && !isParticipant) {
//             throw new org.springframework.web.server.ResponseStatusException(
//                     org.springframework.http.HttpStatus.FORBIDDEN,
//                     "Not allowed to view this conversation");
//         }

//         return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId)
//                 .stream().map(MessageDto::fromEntity).collect(Collectors.toList());
//     }

//     @Transactional
//     public void handleTypingEvent(Integer senderId, TypingEventRequest request) { // <-- THAY ĐỔI
//         firebaseService.pushTypingEvent(request.getConversationId(), senderId, request.getIsTyping());
//     }

//     @Transactional
//     public void handleReadReceipt(Integer senderId, ReadReceiptRequest request) { // <-- THAY ĐỔI
//         Message message = messageRepo.findById(request.getMessageId())
//                 .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
//         if (message.getReadAt() == null) {
//             message.setReadAt(Instant.now());
//             messageRepo.save(message);

//             MessageDto messageDto = MessageDto.fromEntity(message);
//             firebaseService.pushReadReceipt(message.getConversation().getId(), messageDto);
//         }
//     }
// }

package com.example.backend.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.domain.entity.Conversation;
import com.example.backend.domain.entity.ConversationParticipant;
import com.example.backend.domain.entity.Message;
import com.example.backend.dto.chat.ConversationSummaryDto;
import com.example.backend.dto.chat.MessageDto;
import com.example.backend.dto.chat.ReadReceiptRequest;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.dto.chat.TypingEventRequest;
import com.example.backend.repository.ConversationParticipantRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.MessageRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository participantRepo;
    private final FirebaseChatService firebaseService;
    private final UserService userService; // injected

    /**
     * Gửi tin nhắn - enforce business rule: hệ thống chỉ có 1 admin duy nhất.
     */
    public MessageDto sendMessage(Integer senderId, SendMessageRequest request) {
        Integer receiverId = request.getReceiverId();
        if (receiverId == null) {
            throw new IllegalArgumentException("receiverId is required");
        }

        Integer adminId = userService.getAdminUserId();
        boolean senderIsAdmin = userService.isAdmin(senderId);
        boolean receiverIsAdmin = userService.isAdmin(receiverId);

        // Quy tắc hệ thống: có 1 admin duy nhất: mọi customer chỉ chat với admin.
        // 1) Nếu sender là customer => receiver phải là admin.
        if (!senderIsAdmin && !adminId.equals(receiverId)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Customers can only send messages to admin");
        }

        // 2) Nếu sender là admin => receiver phải là customer (không cho admin gửi cho admin)
        if (senderIsAdmin && receiverIsAdmin) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Admin cannot send message to admin");
        }

        if (senderId==receiverId) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Cannot send message to yourself");
        }

        // Tìm conversation giữa admin và customer (dựa trên adminId và customerId)
        Integer customerId = senderIsAdmin ? receiverId : senderId;

        List<Integer> convIds = conversationRepo.findConversationIdsBetweenAdminAndCustomer(adminId, customerId);
        Conversation conversation;
        if (!convIds.isEmpty()) {
            conversation = conversationRepo.findById(convIds.get(0)).orElseThrow();
        } else {
            // Tạo conversation mới chỉ với 2 participant: admin + customer
            conversation = new Conversation();
            conversation = conversationRepo.save(conversation);

            ConversationParticipant adminPart = new ConversationParticipant();
            adminPart.setConversation(conversation);
            adminPart.setUserId(adminId);
            participantRepo.save(adminPart);

            ConversationParticipant customerPart = new ConversationParticipant();
            customerPart.setConversation(conversation);
            customerPart.setUserId(customerId);
            participantRepo.save(customerPart);
        }

        // Create message (senderId là người gửi)
        Message message = new Message(conversation, senderId, request.getMessageType(), request.getContent());
        messageRepo.save(message);

        conversation.setLastMessageAt(Instant.now());
        conversationRepo.save(conversation);

        MessageDto messageDto = MessageDto.fromEntity(message);
        firebaseService.pushNewMessage(messageDto);

        return messageDto;
    }

    public List<MessageDto> getMessageHistory(Integer currentUserId, Integer conversationId) {
        Conversation conv = conversationRepo.findById(conversationId)
                .orElseThrow(() -> new EntityNotFoundException("Conversation not found"));

        boolean isAdmin = userService.isAdmin(currentUserId);
        boolean isParticipant = participantRepo.existsByConversationIdAndUserId(conversationId, currentUserId);

        if (!isAdmin && !isParticipant) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Not allowed to view this conversation");
        }

        return messageRepo.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream().map(MessageDto::fromEntity).collect(Collectors.toList());
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

    @Transactional(readOnly = true)
    public Page<ConversationSummaryDto> getAllConversationsForAdmin(Pageable pageable) {
        // 1) page conversations
        Page<Conversation> convPage = conversationRepo.findAll(pageable);

        List<Integer> convIds = convPage.stream().map(Conversation::getId).collect(Collectors.toList());

        // 2) For simplicity (and because you asked to avoid another repo), we query last message per conversation one-by-one.
        //    This is N+1 on messages but acceptable for small volumes. Optimize later if necessary.
        Map<Integer, MessageDto> lastMessageMap = new HashMap<>();
        for (Integer cid : convIds) {
            messageRepo.findFirstByConversationIdOrderByCreatedAtDesc(cid)
                .ifPresent(m -> lastMessageMap.put(cid, MessageDto.fromEntity(m)));
        }

        // 3) build DTO page content
        List<ConversationSummaryDto> content = convPage.stream().map(conv -> {
            List<Integer> participantIds = Optional.ofNullable(conv.getParticipants())
    .orElse(Collections.emptySet())
    .stream()
    .map(ConversationParticipant::getUserId)
    .collect(Collectors.toList());


            MessageDto last = lastMessageMap.get(conv.getId());
            Long lastAt = last != null ? last.getCreatedAt() : null;

            return ConversationSummaryDto.builder()
                        .conversationId(conv.getId())
                        .participantIds(participantIds)
                        .lastMessage(last)
                        .lastMessageAt(lastAt) // <-- đã là Long
                        .build();
            }).collect(Collectors.toList());
        return new PageImpl<>(content, pageable, convPage.getTotalElements());
    }

    // helper to quickly build Pageable from page/size/sort strings (optional)
    public static Pageable buildPageable(int page, int size, String sortProperty, String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, sortProperty));
    }

}
