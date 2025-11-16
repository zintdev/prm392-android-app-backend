// package com.example.backend.repository;

// import com.example.backend.domain.entity.Conversation;
// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param; // <-- THÊM IMPORT NÀY
// import java.time.Instant;
// import java.util.List;

// public interface ConversationRepository extends JpaRepository<Conversation, Integer> { 

//     /**
//      * SỬA LỖI: Thêm @Param("userId1") và @Param("userId2")
//      * để liên kết các biến Java với các tên tham số trong query.
//      */
//     @Query("SELECT p.conversation.id " +
//            "FROM ConversationParticipant p " +
//            "WHERE p.userId = :userId1 " +
//            "  AND p.conversation.id IN ( " +
//            "    SELECT p2.conversation.id " +
//            "    FROM ConversationParticipant p2 " +
//            "    WHERE p2.userId = :userId2 " +
//            "  ) " +
//            "  AND ( " +
//            "    SELECT COUNT(p3.id) " +
//            "    FROM ConversationParticipant p3 " +
//            "    WHERE p3.conversation.id = p.conversation.id " +
//            "  ) = 2")
//     List<Integer> findConversationIdsBetweenUsers(
//             @Param("userId1") Integer userId1,  // <-- SỬA Ở ĐÂY
//             @Param("userId2") Integer userId2   // <-- SỬA Ở ĐÂY
//     );

//     List<Conversation> findByLastMessageAtBefore(Instant time);
// }

package com.example.backend.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.domain.entity.Conversation;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    /**
     * Tìm conversation id chứa EXACTLY adminId và customerId (2 participants).
     * Trả về list để an toàn nếu có hơn 1 (sẽ lấy index 0).
     */
    @Query(value = """
            SELECT cp.conversation_id
            FROM conversation_participants cp
            WHERE cp.user_id IN (:adminId, :customerId)
            GROUP BY cp.conversation_id
            HAVING COUNT(DISTINCT cp.user_id) = 2
              AND (SELECT COUNT(*) FROM conversation_participants cp2 WHERE cp2.conversation_id = cp.conversation_id) = 2
            """, nativeQuery = true)
    List<Integer> findConversationIdsBetweenAdminAndCustomer(@Param("adminId") Integer adminId,
            @Param("customerId") Integer customerId);

    /**
     * Tìm tất cả conversations có admin tham gia (cho admin dashboard)
     */
    @Query(value = """
            SELECT DISTINCT c.id, c.created_at, c.last_message_at
            FROM conversations c
            INNER JOIN conversation_participants cp ON c.id = cp.conversation_id
            WHERE cp.user_id = :adminId
            ORDER BY c.last_message_at DESC NULLS LAST, c.created_at DESC
            """, nativeQuery = true)
    List<Object[]> findConversationsWithAdmin(@Param("adminId") Integer adminId);

    /**
     * Lấy thông tin customer và unread count cho conversation
     */
    @Query(value = """
            SELECT 
                u.user_id as customerId,
                u.username as customerName,
                u.email as customerEmail,
                COALESCE(unread.unread_count, 0) as unreadCount
            FROM conversation_participants cp
            INNER JOIN users u ON cp.user_id = u.user_id
            LEFT JOIN (
                SELECT 
                    m.conversation_id,
                    COUNT(*) as unread_count
                FROM messages m
                LEFT JOIN message_reads mr ON m.id = mr.message_id AND mr.user_id = :adminId
                WHERE m.sender_id != :adminId
                AND mr.message_id IS NULL
                GROUP BY m.conversation_id
            ) unread ON cp.conversation_id = unread.conversation_id
            WHERE cp.conversation_id = :conversationId
            AND cp.user_id != :adminId
            LIMIT 1
            """, nativeQuery = true)
        List<Object[]> findCustomerInfoByConversationId(@Param("conversationId") Integer conversationId, 
                                                   @Param("adminId") Integer adminId);

    /**
     * Tìm conversations có admin tham gia và customer có tên khớp với tên tìm kiếm
     */
    @Query("""
        select distinct c
        from Conversation c
        join c.participants pAdmin on pAdmin.userId = :adminId
        join c.participants pCustomer
        join User u on u.id = pCustomer.userId
        where pCustomer.userId != :adminId
        and lower(u.username) like lower(concat('%', :name, '%'))
        order by c.lastMessageAt desc nulls last, c.createdAt desc
    """)
    Page<Conversation> findByParticipantUserFullNameLikeIgnoreCase(
        @Param("name") String name, 
        @Param("adminId") Integer adminId,
        Pageable pageable);

}