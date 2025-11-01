package com.example.backend.repository;

import com.example.backend.domain.entity.MessageRead;
import com.example.backend.domain.entity.MessageReadId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageReadRepository extends JpaRepository<MessageRead, MessageReadId> {
    
    boolean existsByMessageIdAndUserId(Integer messageId, Integer userId);
    
    @Query("SELECT mr FROM MessageRead mr WHERE mr.messageId IN :messageIds AND mr.userId = :userId")
    List<MessageRead> findByMessageIdInAndUserId(@Param("messageIds") List<Integer> messageIds, @Param("userId") Integer userId);
    
    @Query("SELECT mr.messageId FROM MessageRead mr WHERE mr.messageId IN :messageIds AND mr.userId = :userId")
    List<Integer> findReadMessageIds(@Param("messageIds") List<Integer> messageIds, @Param("userId") Integer userId);
}
