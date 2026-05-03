package com.example.svmarket.repository;

import com.example.svmarket.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Integer> {

    Optional<Conversation> findByBuyerIdAndSellerIdAndListingId(Integer buyerId, Integer sellerId, Integer listingId);

    @Query("""
            SELECT c FROM Conversation c
            WHERE c.buyer.id = :userId OR c.seller.id = :userId
            ORDER BY c.updatedAt DESC
            """)
    List<Conversation> findByParticipantIdOrderByUpdatedAtDesc(@Param("userId") Integer userId);
}
