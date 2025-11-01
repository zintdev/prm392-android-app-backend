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
import com.example.backend.dto.chat.ConversationDto;
import com.example.backend.dto.chat.ConversationSummaryDto;
import com.example.backend.dto.chat.MessageDto;
import com.example.backend.dto.chat.ReadReceiptRequest;
import com.example.backend.dto.chat.SendMessageRequest;
import com.example.backend.dto.chat.TypingEventRequest;
import com.example.backend.mapper.ConversationMapper;
import com.example.backend.repository.ConversationParticipantRepository;
import com.example.backend.repository.ConversationRepository;
import com.example.backend.repository.MessageRepository;
import com.example.backend.repository.MessageReadRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChatService {

    private final MessageRepository messageRepo;
    private final ConversationRepository conversationRepo;
    private final ConversationParticipantRepository participantRepo;
    private final MessageReadRepository messageReadRepo;
    private final FirebaseChatService firebaseService;
    private final UserService userService; // injected
    private final ConversationMapper conversationMapper;

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
        
        // Push to Firebase asynchronously (don't wait for completion)
        firebaseService.pushNewMessage(messageDto)
            .exceptionally(throwable -> {
                log.error("Failed to push message to Firebase: {}", throwable.getMessage());
                return null; // Continue execution even if Firebase fails
            });

        // --- THÊM MỚI: Bắt đầu từ đây ---
        // 1. Tạo bản tóm tắt cho Admin Inbox
        ConversationSummaryDto summaryDto = buildSummaryForAdmin(conversation.getId(), adminId, customerId, messageDto);
        
        // 2. Đẩy (push) bản tóm tắt này lên node riêng cho Admin
        // (Hàm này cần được thêm vào FirebaseChatService)
        firebaseService.pushAdminInboxUpdate(adminId, summaryDto)
            .exceptionally(throwable -> {
                log.error("Failed to push ADMIN INBOX update to Firebase: {}", throwable.getMessage());
                return null;
            });
        // --- THÊM MỚI: Kết thúc ở đây ---

        return messageDto;
    }

    /**
     * THÊM MỚI: Hàm helper để tạo DTO tóm tắt cho Admin
     */
    private ConversationSummaryDto buildSummaryForAdmin(Integer conversationId, Integer adminId, Integer customerId, MessageDto lastMessage) {
        // 1. Lấy thông tin Customer và số tin chưa đọc
        // (Tái sử dụng native query hiệu quả của bạn)
        List<Object[]> customerData = conversationRepo.findCustomerInfoByConversationId(conversationId, adminId);
        
        String customerName = null;
        String customerEmail = null;
        Integer unreadCount = 0;

        if (!customerData.isEmpty()) {
            Object[] row = customerData.get(0);
            try {
                customerName = row[1] != null ? row[1].toString() : null;
                customerEmail = row[2] != null ? row[2].toString() : null;
                Object unreadObj = row[3];
                if (unreadObj instanceof Number) {
                    unreadCount = ((Number) unreadObj).intValue();
                } else {
                    unreadCount = Integer.parseInt(unreadObj.toString());
                }
            } catch (Exception e) {
                log.warn("Error parsing customer data for summary push", e);
            }
        }

        // 2. Xây dựng DTO tóm tắt
        return ConversationSummaryDto.builder()
                .conversationId(conversationId)
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessage.getCreatedAt())
                .customerId(customerId)
                .customerName(customerName)
                .customerEmail(customerEmail)
                .unreadCount(unreadCount) // Đã bao gồm số tin chưa đọc mới nhất
                .build();
    }

    public List<MessageDto> getMessageHistory(Integer currentUserId, Integer conversationId) {
        // Verify conversation exists
        if (!conversationRepo.existsById(conversationId)) {
            throw new EntityNotFoundException("Conversation not found");
        }

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
    public void handleTypingEvent(Integer senderId, TypingEventRequest request) {
        // Push to Firebase asynchronously
        firebaseService.pushTypingEvent(request.getConversationId(), senderId, request.getIsTyping())
            .exceptionally(throwable -> {
                log.error("Failed to push typing event to Firebase: {}", throwable.getMessage());
                return null;
            });
    }

    @Transactional
    public void handleReadReceipt(Integer senderId, ReadReceiptRequest request) {
        Message message = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> new EntityNotFoundException("Message not found"));
        
        // Check if already read by this user
        if (!messageReadRepo.existsByMessageIdAndUserId(request.getMessageId(), senderId)) {
            // Create read receipt
            com.example.backend.domain.entity.MessageRead messageRead = 
                com.example.backend.domain.entity.MessageRead.builder()
                    .messageId(request.getMessageId())
                    .userId(senderId)
                    .readAt(Instant.now())
                    .build();
            messageReadRepo.save(messageRead);

            MessageDto messageDto = MessageDto.fromEntity(message);
            
            // Push to Firebase asynchronously
            firebaseService.pushReadReceipt(message.getConversation().getId(), messageDto)
                .exceptionally(throwable -> {
                    log.error("Failed to push read receipt to Firebase: {}", throwable.getMessage());
                    return null;
                });
        }
    }

    @Transactional(readOnly = true)
public Page<ConversationSummaryDto> getAllConversationsForAdmin(Pageable pageable) {
    Integer adminId = userService.getAdminUserId();
    if (adminId == null) {
        throw new EntityNotFoundException("Admin not found");
    }

    // 1) Get conversations with admin using optimized query
    List<Object[]> conversationData = conversationRepo.findConversationsWithAdmin(adminId);

    // 2) Extract conversation IDs safely
    List<Integer> convIds = conversationData.stream()
        .map(row -> {
            Object idObj = row[0];
            if (idObj == null) return null;
            if (idObj instanceof Number) return ((Number) idObj).intValue();
            return Integer.valueOf(idObj.toString());
        })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    // 3) Get last messages for all conversations in one query
    final Map<Integer, MessageDto> lastMessageMap = new HashMap<>();
    if (!convIds.isEmpty()) {
        List<Message> lastMessages = messageRepo.findLastMessagesByConversationIds(convIds);
        Map<Integer, MessageDto> tempMap = lastMessages.stream()
            .filter(Objects::nonNull)
            .collect(Collectors.toMap(
                m -> m.getConversation().getId(),
                MessageDto::fromEntity,
                (existing, replacement) -> {
                    // keep the one with greater createdAt
                    if (existing.getCreatedAt() == null) return replacement;
                    if (replacement.getCreatedAt() == null) return existing;
                    return existing.getCreatedAt() >= replacement.getCreatedAt() ? existing : replacement;
                }
            ));
        lastMessageMap.putAll(tempMap);
    }

    // 4) Get customer info for each conversation — do parsing safely
    Map<Integer, Map<String, Object>> customerInfoMap = new HashMap<>();
    for (Integer convId : convIds) {
        List<Object[]> customerData = conversationRepo.findCustomerInfoByConversationId(convId, adminId);
        if (!customerData.isEmpty()) {
            Object[] row = customerData.get(0);
            Map<String, Object> customerInfo = new HashMap<>();

            // parse customerId
            Object custIdObj = row[0];
            Integer customerId = null;
            if (custIdObj != null) {
                if (custIdObj instanceof Number) customerId = ((Number) custIdObj).intValue();
                else customerId = Integer.valueOf(custIdObj.toString());
            }
            customerInfo.put("customerId", customerId);

            // parse customerName/email (strings)
            customerInfo.put("customerName", row[1] != null ? row[1].toString() : null);
            customerInfo.put("customerEmail", row[2] != null ? row[2].toString() : null);

            // parse unreadCount (could be BigInteger/Long/Integer)
            Object unreadObj = row[3];
            Integer unreadCount = 0;
            if (unreadObj != null) {
                if (unreadObj instanceof Number) unreadCount = ((Number) unreadObj).intValue();
                else {
                    try { unreadCount = Integer.parseInt(unreadObj.toString()); }
                    catch (NumberFormatException ex) { unreadCount = 0; }
                }
            }
            customerInfo.put("unreadCount", unreadCount);

            customerInfoMap.put(convId, customerInfo);
        }
    }

    // 5) Build DTOs - parse created_at / last_message_at from conversationData safely
    List<ConversationSummaryDto> content = conversationData.stream()
        .map(row -> {
            // row layout: c.id, c.created_at, c.last_message_at (per your query)
            Integer convId = null;
            Object idObj = row[0];
            if (idObj != null) {
                if (idObj instanceof Number) convId = ((Number) idObj).intValue();
                else convId = Integer.valueOf(idObj.toString());
            }

            // parse last_message_at into epoch millis (Long) or null
            Long lastMsgAt = null;
            Object lastMsgAtObj = row.length > 2 ? row[2] : null;
            if (lastMsgAtObj != null) {
                if (lastMsgAtObj instanceof Number) {
                    lastMsgAt = ((Number) lastMsgAtObj).longValue();
                } else if (lastMsgAtObj instanceof java.sql.Timestamp) {
                    lastMsgAt = ((java.sql.Timestamp) lastMsgAtObj).toInstant().toEpochMilli();
                } else {
                    // try parse string
                    try {
                        lastMsgAt = Instant.parse(lastMsgAtObj.toString()).toEpochMilli();
                    } catch (Exception ignored) {
                        try { lastMsgAt = Long.parseLong(lastMsgAtObj.toString()); } catch (Exception e) { lastMsgAt = null; }
                    }
                }
            }

            MessageDto lastMessage = lastMessageMap.get(convId);
            Map<String, Object> customerInfo = customerInfoMap.get(convId);

            return ConversationSummaryDto.builder()
                .conversationId(convId)
                .lastMessage(lastMessage)
                .lastMessageAt(lastMessage != null && lastMessage.getCreatedAt() != null ? lastMessage.getCreatedAt() : lastMsgAt)
                .customerId(customerInfo != null ? (Integer) customerInfo.get("customerId") : null)
                .customerName(customerInfo != null ? (String) customerInfo.get("customerName") : null)
                .customerEmail(customerInfo != null ? (String) customerInfo.get("customerEmail") : null)
                .unreadCount(customerInfo != null ? (Integer) customerInfo.get("unreadCount") : 0)
                .build();
        })
        .collect(Collectors.toList());

    // 6) Apply pagination manually (since we're using native query)
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), content.size());
    List<ConversationSummaryDto> pageContent = content.subList(start, end);

    return new PageImpl<>(pageContent, pageable, content.size());
}


    /**
     * Customer lấy conversation với admin
     */
    @Transactional(readOnly = true)
    public ConversationDto getCustomerConversationWithAdmin(Integer customerId) {
        Integer adminId = userService.getAdminUserId();
        if (adminId == null) {
            throw new EntityNotFoundException("Admin not found");
        }
        
        List<Integer> convIds = conversationRepo.findConversationIdsBetweenAdminAndCustomer(adminId, customerId);
        if (!convIds.isEmpty()) {
            Conversation conversation = conversationRepo.findById(convIds.get(0)).orElseThrow();
            return ConversationDto.fromEntity(conversation);
        }
        
        // Tạo conversation mới nếu chưa có
        Conversation conversation = new Conversation();
        conversation = conversationRepo.save(conversation);

        ConversationParticipant adminPart = new ConversationParticipant();
        adminPart.setConversation(conversation);
        adminPart.setUserId(adminId);
        participantRepo.save(adminPart);

        ConversationParticipant customerPart = new ConversationParticipant();
        customerPart.setConversation(conversation);
        customerPart.setUserId(customerId);
        participantRepo.save(customerPart);
        
        return ConversationDto.fromEntity(conversation);
    }

    /**
     * Lấy số tin nhắn chưa đọc cho customer
     */
    @Transactional(readOnly = true)
    public Integer getUnreadCountForCustomer(Integer customerId, Integer conversationId) {
        return messageRepo.countUnreadMessagesForUser(conversationId, customerId);
    }

    // helper to quickly build Pageable from page/size/sort strings (optional)
    public static Pageable buildPageable(int page, int size, String sortProperty, String direction) {
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, sortProperty));
    }

    public Page<ConversationSummaryDto> searchConversationsByCustomerNameForAdmin(String customerName, Pageable pageable, Integer adminId) {
        Page<Conversation> page = conversationRepo.findByParticipantUserFullNameLikeIgnoreCase(customerName, adminId, pageable);

        // 1) Map Conversation -> ConversationSummaryDto (without unreadCount)
        Page<ConversationSummaryDto> dtoPage = page.map(conversationMapper::toSummaryDto);

        // 2) Collect conversationIds to batch-query unread counts
        List<Integer> convIds = dtoPage.getContent().stream()
                .map(ConversationSummaryDto::getConversationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (!convIds.isEmpty()) {
            // call batch count
            List<Object[]> raw = messageRepo.countUnreadByConversationIdsAndReceiverNative(convIds, adminId);

            // build map convId -> count
            Map<Integer, Integer> unreadMap = new HashMap<>();
            for (Object[] row : raw) {
                if (row == null || row.length < 2) continue;
                Integer convId = row[0] instanceof Number ? ((Number) row[0]).intValue() : Integer.parseInt(String.valueOf(row[0]));
                Integer cnt = row[1] instanceof Number ? ((Number) row[1]).intValue() : Integer.parseInt(String.valueOf(row[1]));
                unreadMap.put(convId, cnt);
            }

            // 3) Set unreadCount on DTOs (mutate dtoPage content)
            dtoPage.getContent().forEach(d -> {
                Integer cid = d.getConversationId();
                if (cid != null) {
                    d.setUnreadCount(unreadMap.getOrDefault(cid, 0));
                } else {
                    d.setUnreadCount(0);
                }
            });
        } else {
            // no conversations
            dtoPage.getContent().forEach(d -> d.setUnreadCount(0));
        }

        return dtoPage;
    }

}
