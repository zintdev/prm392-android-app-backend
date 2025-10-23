package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.domain.entity.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> { // <-- THAY ĐỔI
    List<Message> findByConversationIdOrderByCreatedAtAsc(Integer conversationId); // <-- THAY ĐỔI
    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Integer conversationId); // <-- THAY ĐỔI
}