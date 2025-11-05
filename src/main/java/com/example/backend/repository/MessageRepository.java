package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.Message;

import java.util.List;
import java.util.Optional;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(Integer conversationId);

    Optional<Message> findFirstByConversationIdOrderByCreatedAtDesc(Integer conversationId);

    /**
     * Get last message for multiple conversations in one query (native - Postgres DISTINCT ON)
     */
    @Query(value = """
            SELECT DISTINCT ON (m.conversation_id) m.*
            FROM messages m
            WHERE m.conversation_id IN :conversationIds
            ORDER BY m.conversation_id, m.created_at DESC
            """, nativeQuery = true)
    List<Message> findLastMessagesByConversationIds(@Param("conversationIds") List<Integer> conversationIds);

    /**
     * Count unread messages for a user in a conversation (native)
     * Uses message_reads table (left join + mr.user_id) — same logic as your other native query.
     */
    @Query(value = """
            SELECT COUNT(*)
            FROM messages m
            LEFT JOIN message_reads mr ON m.id = mr.message_id AND mr.user_id = :userId
            WHERE m.conversation_id = :conversationId
              AND m.sender_id != :userId
              AND mr.message_id IS NULL
            """, nativeQuery = true)
    Integer countUnreadMessagesForUser(@Param("conversationId") Integer conversationId,
                                       @Param("userId") Integer userId);

    /**
     * Batch count unread per conversation using native SQL (recommended)
     * Returns list of Object[] where [0]=conversation_id, [1]=count
     *
     * Make sure column names (conversation_id, read_at, receiver_id) match your DB schema.
     */
    @Query(value = """
        SELECT m.conversation_id AS convId, COUNT(*) AS cnt
        FROM messages m
        LEFT JOIN message_reads mr ON m.id = mr.message_id AND mr.user_id = :receiverId
        WHERE m.conversation_id IN (:convIds)
          AND mr.message_id IS NULL
          AND m.sender_id != :receiverId
        GROUP BY m.conversation_id
        """, nativeQuery = true)
    List<Object[]> countUnreadByConversationIdsAndReceiverNative(@Param("convIds") List<Integer> convIds,
                                                                 @Param("receiverId") Integer receiverId);
}
