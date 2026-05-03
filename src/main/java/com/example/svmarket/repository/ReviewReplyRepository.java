package com.example.svmarket.repository;

import com.example.svmarket.entity.ReviewReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewReplyRepository extends JpaRepository<ReviewReply, Integer> {
    boolean existsByReviewId(Integer reviewId);
    Optional<ReviewReply> findByReviewId(Integer reviewId);
}