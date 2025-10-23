package com.example.backend.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.example.backend.dto.chat.FirebaseTypingEvent;
import com.example.backend.dto.chat.MessageDto;
import org.springframework.stereotype.Service;

@Service
public class FirebaseChatService {

    private final DatabaseReference database;

    public FirebaseChatService(FirebaseApp firebaseApp) {
        this.database = FirebaseDatabase.getInstance(firebaseApp).getReference();
    }

    private DatabaseReference getMessagesRef(Integer conversationId) { // <-- THAY ĐỔI
        return database.child("messages").child(String.valueOf(conversationId));
    }

    private DatabaseReference getEventsRef(Integer conversationId) { // <-- THAY ĐỔI
        return database.child("events").child(String.valueOf(conversationId));
    }

    public void pushNewMessage(MessageDto messageDto) {
        getMessagesRef(messageDto.getConversationId()).push().setValueAsync(messageDto);
    }

    public void pushTypingEvent(Integer conversationId, Integer userId, boolean isTyping) { // <-- THAY ĐỔI
        FirebaseTypingEvent event = new FirebaseTypingEvent(userId, isTyping);
        getEventsRef(conversationId).child("typing").child(String.valueOf(userId)).setValueAsync(event);
    }

    public void pushReadReceipt(Integer conversationId, MessageDto messageDto) { // <-- THAY ĐỔI
        getEventsRef(conversationId).child("read").push().setValueAsync(messageDto);
    }
    
    public DatabaseReference getPresenceRef(Integer userId) { // <-- THAY ĐỔI
         return database.child("status").child(String.valueOf(userId));
    }
}