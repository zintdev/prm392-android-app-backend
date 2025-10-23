package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.ConversationParticipant;

import java.util.List;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Integer> { // <-- THAY ĐỔI
    List<ConversationParticipant> findByUserId(Integer userId); // <-- THAY ĐỔI
    List<ConversationParticipant> findByConversationId(Integer conversationId); // <-- THAY ĐỔI
}