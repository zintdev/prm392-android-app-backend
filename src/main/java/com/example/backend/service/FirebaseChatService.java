package com.example.backend.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseException;
import com.example.backend.dto.chat.ConversationSummaryDto;
import com.example.backend.dto.chat.FirebaseTypingEvent;
import com.example.backend.dto.chat.MessageDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FirebaseChatService {

    private final DatabaseReference database;

    public FirebaseChatService(FirebaseApp firebaseApp) {
        this.database = FirebaseDatabase.getInstance(firebaseApp).getReference();
    }

    private DatabaseReference getMessagesRef(Integer conversationId) {
        return database.child("messages").child(String.valueOf(conversationId));
    }

    private DatabaseReference getEventsRef(Integer conversationId) {
        return database.child("events").child(String.valueOf(conversationId));
    }

    /**
     * Push new message to Firebase - async operation
     */
    public CompletableFuture<Void> pushNewMessage(MessageDto messageDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                getMessagesRef(messageDto.getConversationId())
                    .push()
                    .setValueAsync(messageDto)
                    .get(); // Wait for completion
                log.debug("Message pushed to Firebase: conversationId={}, messageId={}", 
                    messageDto.getConversationId(), messageDto.getId());
            } catch (Exception e) {
                log.error("Failed to push message to Firebase: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to push message to Firebase", e);
            }
        });
    }

    public CompletableFuture<Void> pushAdminInboxUpdate(Integer adminId, ConversationSummaryDto summaryDto) {
        if (adminId == null || summaryDto == null || summaryDto.getConversationId() == null) {
            // Không làm gì nếu thiếu thông tin
            return CompletableFuture.completedFuture(null);
        }

        // Đường dẫn: admin_inbox_updates/{ADMIN_ID}/{CONVERSATION_ID}
        DatabaseReference adminUpdateRef = database.child("admin_inbox_updates")
                                                  .child(String.valueOf(adminId))
                                                  .child(String.valueOf(summaryDto.getConversationId()));

        // Ghi đè (setValue) bản tóm tắt mới nhất cho cuộc hội thoại này
        return CompletableFuture.runAsync(() -> adminUpdateRef.setValueAsync(summaryDto));
    }

    /**
     * Push typing event to Firebase - async operation
     */
    public CompletableFuture<Void> pushTypingEvent(Integer conversationId, Integer userId, boolean isTyping) {
        return CompletableFuture.runAsync(() -> {
            try {
                FirebaseTypingEvent event = new FirebaseTypingEvent(userId, isTyping);
                getEventsRef(conversationId)
                    .child("typing")
                    .child(String.valueOf(userId))
                    .setValueAsync(event)
                    .get(); // Wait for completion
                log.debug("Typing event pushed to Firebase: conversationId={}, userId={}, isTyping={}", 
                    conversationId, userId, isTyping);
            } catch (Exception e) {
                log.error("Failed to push typing event to Firebase: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to push typing event to Firebase", e);
            }
        });
    }

    /**
     * Push read receipt to Firebase - async operation
     */
    public CompletableFuture<Void> pushReadReceipt(Integer conversationId, MessageDto messageDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                getEventsRef(conversationId)
                    .child("read")
                    .push()
                    .setValueAsync(messageDto)
                    .get(); // Wait for completion
                log.debug("Read receipt pushed to Firebase: conversationId={}, messageId={}", 
                    conversationId, messageDto.getId());
            } catch (Exception e) {
                log.error("Failed to push read receipt to Firebase: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to push read receipt to Firebase", e);
            }
        });
    }
    
    /**
     * Get presence reference for user status
     */
    public DatabaseReference getPresenceRef(Integer userId) {
         return database.child("status").child(String.valueOf(userId));
    }

    /**
     * Update user presence status
     */
    public CompletableFuture<Void> updateUserPresence(Integer userId, boolean isOnline) {
        return CompletableFuture.runAsync(() -> {
            try {
                getPresenceRef(userId)
                    .child("online")
                    .setValueAsync(isOnline)
                    .get();
                log.debug("User presence updated: userId={}, isOnline={}", userId, isOnline);
            } catch (Exception e) {
                log.error("Failed to update user presence: {}", e.getMessage(), e);
                throw new RuntimeException("Failed to update user presence", e);
            }
        });
    }
}