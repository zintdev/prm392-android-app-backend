// package com.example.backend.repository;

// import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.data.jpa.repository.Query;
// import org.springframework.data.repository.query.Param;

// import com.example.backend.domain.entity.ConversationParticipant;

// import java.util.List;

// public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Integer> { // <-- THAY
//                                                                                                              // ĐỔI
//     List<ConversationParticipant> findByUserId(Integer userId); // <-- THAY ĐỔI

//     List<ConversationParticipant> findByConversationId(Integer conversationId); // <-- THAY ĐỔI

//     @Query(value = """
//             SELECT cp.conversation_id
//             FROM conversation_participants cp
//             WHERE cp.user_id IN (:a, :b)
//             GROUP BY cp.conversation_id
//             HAVING COUNT(DISTINCT cp.user_id) = 2
//             """, nativeQuery = true)
//     List<Integer> findConversationIdsBetweenUsers(@Param("a") Integer a, @Param("b") Integer b);

//     boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);

// }


package com.example.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.backend.domain.entity.ConversationParticipant;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, Integer> {
    boolean existsByConversationIdAndUserId(Integer conversationId, Integer userId);
}
