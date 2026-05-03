package com.example.svmarket.repository;

import com.example.svmarket.entity.Message;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(Integer conversationId);

    @Query("""
            SELECT COUNT(m) FROM Message m
            WHERE m.conversation.id = :conversationId
              AND m.sender.id <> :userId
              AND (m.isRead = false OR m.isRead IS NULL)
            """)
    long countUnreadMessages(@Param("conversationId") Integer conversationId, @Param("userId") Integer userId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Message m
            SET m.isRead = true
            WHERE m.conversation.id = :conversationId
              AND m.sender.id <> :userId
              AND (m.isRead = false OR m.isRead IS NULL)
            """)
    int markConversationAsRead(@Param("conversationId") Integer conversationId, @Param("userId") Integer userId);
}
