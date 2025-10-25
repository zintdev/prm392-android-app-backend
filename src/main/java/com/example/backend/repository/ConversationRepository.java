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
}