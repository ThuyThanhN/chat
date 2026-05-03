package com.example.svmarket.repository;

import com.example.svmarket.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    boolean existsByOrderId(Integer orderId);
    boolean existsByReviewerIdAndRevieweeId(Integer reviewerId, Integer revieweeId);
    List<Review> findByRevieweeIdOrderByCreatedAtDesc(Integer revieweeId);
    Optional<Review> findByOrderIdAndReviewerId(Integer orderId, Integer reviewerId);
}