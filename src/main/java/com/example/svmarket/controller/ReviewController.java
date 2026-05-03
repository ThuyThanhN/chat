package com.example.svmarket.controller;

import com.example.svmarket.dto.BuyerReviewDTO;
import com.example.svmarket.dto.ReviewReplyRequest;
import com.example.svmarket.dto.ReviewRequest;
import com.example.svmarket.dto.ReviewableTransactionDTO;
import com.example.svmarket.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ReviewController {
    private final ReviewService reviewService;

    @GetMapping("/reviewable-transactions")
    public ResponseEntity<List<ReviewableTransactionDTO>> getReviewableTransactions() {
        return ResponseEntity.ok(reviewService.getReviewableTransactions());
    }

    @PostMapping
    public ResponseEntity<?> submitReview(@RequestBody ReviewRequest request) {
        try {
            reviewService.submitReview(request);
            return ResponseEntity.ok("Đánh giá thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buyer-reviews")
    public ResponseEntity<List<BuyerReviewDTO>> getBuyerReviews() {
        return ResponseEntity.ok(reviewService.getBuyerReviews());
    }

    @PostMapping("/reply")
    public ResponseEntity<?> replyToReview(@RequestBody ReviewReplyRequest request) {
        try {
            reviewService.replyToReview(request);
            return ResponseEntity.ok("Phản hồi thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{reviewId}/reply")
    public ResponseEntity<?> getReviewReply(@PathVariable Integer reviewId) {
        try {
            return ResponseEntity.ok(reviewService.getReviewReply(reviewId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/order/{orderId}/detail")
    public ResponseEntity<?> getMyReviewDetail(@PathVariable Integer orderId) {
        try {
            return ResponseEntity.ok(reviewService.getMyReviewDetail(orderId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}