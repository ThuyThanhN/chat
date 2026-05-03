package com.example.svmarket.service;

import com.example.svmarket.dto.BuyerReviewDTO;
import com.example.svmarket.dto.ReviewRequest;
import com.example.svmarket.dto.ReviewReplyRequest;
import com.example.svmarket.dto.ReviewReplyDTO;
import com.example.svmarket.dto.ReviewDetailDTO;
import com.example.svmarket.dto.ReviewableTransactionDTO;
import com.example.svmarket.entity.*;
import com.example.svmarket.repository.*;
import com.example.svmarket.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final NotificationRepository notificationRepository;
    private final ReviewReplyRepository reviewReplyRepository;
    private final JwtUtil jwtUtil;

    public List<ReviewableTransactionDTO> getReviewableTransactions() {
        User currentUser = jwtUtil.getCurrentUser();
        
        // Lấy các giao dịch thanh toán SUCCESS của người dùng này
        List<Payment> successfulPayments = paymentRepository
                .findByOrder_BuyerAndStatusOrderByIdDesc(currentUser, PaymentStatus.SUCCESS);

        return successfulPayments.stream().map(payment -> {
            Order order = payment.getOrder();
            
            String productName = "Đơn hàng #" + order.getId();
            Listing listing = null;
            try {
                if (order.getOrderDetails() != null && !order.getOrderDetails().isEmpty()) {
                    listing = order.getOrderDetails().get(0).getListing();
                    productName = listing.getTitle();
                }
            } catch (Exception ignored) { } // Dự phòng lỗi lấy Title

            // Kiểm tra xem đơn hàng này đã được đánh giá chưa
            boolean isReviewed = reviewRepository.existsByOrderId(order.getId());

            return ReviewableTransactionDTO.builder()
                    .orderId(order.getId())
                    .transactionId(payment.getTransactionId() != null ? payment.getTransactionId() : String.valueOf(payment.getId()))
                    .sellerName(order.getSeller() != null ? order.getSeller().getFullName() : "Khuyết danh")
                    .sellerAvatar(order.getSeller() != null ? order.getSeller().getAvatar() : null)
                    .productName(productName)
                    .isReviewed(isReviewed)
                    .build();
        }).collect(Collectors.toList());
    }

    public void submitReview(ReviewRequest request) {
        User currentUser = jwtUtil.getCurrentUser();
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (!order.getBuyer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền đánh giá đơn hàng này");
        }

        Listing listing = order.getOrderDetails() != null && !order.getOrderDetails().isEmpty() 
                ? order.getOrderDetails().get(0).getListing() 
                : null;

        boolean alreadyReviewed = reviewRepository.existsByOrderId(order.getId());

        if (alreadyReviewed) {
            throw new RuntimeException("Bạn đã đánh giá giao dịch này rồi");
        }

        Review review = Review.builder()
                .order(order)
                .listing(listing)
                .reviewer(currentUser)
                .reviewee(order.getSeller())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        reviewRepository.save(review);

        // Tạo và lưu thông báo gửi đến người bán
        String productName = listing != null ? listing.getTitle() : "Đơn hàng #" + order.getId();
        Notification notification = Notification.builder()
                .user(order.getSeller())
                .content(currentUser.getFullName() + " đã đánh giá " + request.getRating() + " sao cho giao dịch " + productName)
                .type(NotificationType.REVIEW)
                .referenceId(order.getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<BuyerReviewDTO> getBuyerReviews() {
        User currentUser = jwtUtil.getCurrentUser();
        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(currentUser.getId());

        return reviews.stream().map(review -> {
            boolean isReplied = reviewReplyRepository.existsByReviewId(review.getId());
            
            Order order = review.getOrder();
            Listing listing = review.getListing();
            
            String transactionId = String.valueOf(order.getId());
            if (order.getPayments() != null && !order.getPayments().isEmpty()) {
                transactionId = order.getPayments().get(0).getTransactionId() != null 
                        ? order.getPayments().get(0).getTransactionId() 
                        : String.valueOf(order.getPayments().get(0).getId());
            }

            return BuyerReviewDTO.builder()
                    .reviewId(review.getId())
                    .orderId(order.getId())
                    .transactionId(transactionId)
                    .buyerName(review.getReviewer().getFullName())
                    .buyerAvatar(review.getReviewer().getAvatar())
                    .productName(listing != null ? listing.getTitle() : "Đơn hàng #" + order.getId())
                    .rating(review.getRating())
                    .comment(review.getComment())
                    .isReplied(isReplied)
                    .createdAt(review.getCreatedAt())
                    .build();
        }).collect(Collectors.toList());
    }

    public void replyToReview(ReviewReplyRequest request) {
        User currentUser = jwtUtil.getCurrentUser();
        Review review = reviewRepository.findById(request.getReviewId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        if (!review.getReviewee().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền phản hồi đánh giá này");
        }
        if (reviewReplyRepository.existsByReviewId(review.getId())) {
            throw new RuntimeException("Bạn đã phản hồi đánh giá này rồi");
        }
        ReviewReply reply = ReviewReply.builder().review(review).replier(currentUser).content(request.getContent()).build();
        reviewReplyRepository.save(reply);

        // Tạo và lưu thông báo gửi đến người mua
        String productName = review.getListing() != null ? review.getListing().getTitle() : "Đơn hàng #" + review.getOrder().getId();
        Notification notification = Notification.builder()
                .user(review.getReviewer())
                .content(currentUser.getFullName() + " đã phản hồi đánh giá của bạn về giao dịch " + productName)
                .type(NotificationType.REVIEW_REPLY)
                .referenceId(review.getOrder().getId())
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public ReviewReplyDTO getReviewReply(Integer reviewId) {
        ReviewReply reply = reviewReplyRepository.findByReviewId(reviewId)
                .orElseThrow(() -> new RuntimeException("Chưa có phản hồi"));
        return ReviewReplyDTO.builder().content(reply.getContent()).createdAt(reply.getCreatedAt()).build();
    }

    public ReviewDetailDTO getMyReviewDetail(Integer orderId) {
        User currentUser = jwtUtil.getCurrentUser();
        Review review = reviewRepository.findByOrderIdAndReviewerId(orderId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        Optional<ReviewReply> replyOpt = reviewReplyRepository.findByReviewId(review.getId());

        return ReviewDetailDTO.builder()
                .sellerAvatar(review.getReviewee().getAvatar())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .replyContent(replyOpt.map(ReviewReply::getContent).orElse(null))
                .replyCreatedAt(replyOpt.map(ReviewReply::getCreatedAt).orElse(null))
                .build();
    }
}