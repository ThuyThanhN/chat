package com.example.svmarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import com.example.svmarket.dto.*;
import com.example.svmarket.entity.*;
import com.example.svmarket.repository.*;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private ListingService listingService;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewReplyRepository reviewReplyRepository;

    // GET PROFILE
    public ProfileResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        Address address = user.getAddresses() != null
                ? user.getAddresses().stream().findFirst().orElse(null)
                : null;

        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatar(),
                user.getUniversity(),
                address != null ? address.getProvince() : "",
                address != null ? address.getAddressDetail() : "",
                user.getGender() != null ? user.getGender().name() : "OTHER",
                user.getIsVerified());
    }

    // UPDATE PROFILE
    public void updateProfile(String email, UpdateProfileRequest req) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setFullName(req.getFullName());
        user.setUniversity(req.getUniversity());

        if (req.getGender() != null) {
            user.setGender(Gender.valueOf(req.getGender().toUpperCase()));
        }

        // ADDRESS HANDLING
        Address address = null;

        if (user.getAddresses() != null && !user.getAddresses().isEmpty()) {
            address = user.getAddresses().get(0);
        }

        if (address == null) {
            address = new Address();
            address.setUser(user);

            if (user.getAddresses() != null) {
                user.getAddresses().add(address);
            }
        }

        address.setProvince(req.getProvince());
        address.setAddressDetail(req.getAddressDetail());

        userRepository.save(user);
    }

    // UPLOAD AVATAR
    public String uploadAvatar(String email, MultipartFile file) {

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            if (file.isEmpty()) {
                throw new RuntimeException("File rỗng");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chỉ được upload ảnh");
            }

            CloudinaryService.UploadedImage uploadedImage = cloudinaryService.uploadAvatarImage(file);
            String avatarUrl = uploadedImage.secureUrl();

            user.setAvatar(avatarUrl);

            userRepository.save(user);

            return avatarUrl;

        } catch (Exception e) {
            throw new RuntimeException("Upload avatar thất bại: " + e.getMessage());
        }
    }

    // GET SELLER PROFILE
    public SellerProfileResponse getSellerProfile(Integer sellerId) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Người bán không tồn tại"));

        List<Listing> listings = seller.getListings() != null ? seller.getListings() : List.of();

        List<ListingSummaryResponse> activeListings = listings.stream()
                .filter(l -> l.getStatus() == com.example.svmarket.entity.ListingStatus.ACTIVE)
                .map(listingService::toSummaryResponse)
                .toList();

        List<ListingSummaryResponse> soldListings = listings.stream()
                .filter(l -> l.getStatus() == com.example.svmarket.entity.ListingStatus.SOLD)
                .map(listingService::toSummaryResponse)
                .toList();

        long soldCount = listings.stream()
                .filter(l -> l.getStatus() == com.example.svmarket.entity.ListingStatus.SOLD)
                .count();

        // Lấy địa chỉ
        Address address = seller.getAddresses() != null
                ? seller.getAddresses().stream().findFirst().orElse(null)
                : null;

        List<Review> reviews = reviewRepository.findByRevieweeIdOrderByCreatedAtDesc(seller.getId());
        double averageRating = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);

        List<ReviewData> reviewDataList = reviews.stream().map(r -> {
            String pName = "Sản phẩm";
            if (r.getListing() != null) {
                pName = r.getListing().getTitle();
            } else if (r.getOrder() != null && r.getOrder().getOrderDetails() != null && !r.getOrder().getOrderDetails().isEmpty()) {
                pName = r.getOrder().getOrderDetails().get(0).getListing().getTitle();
            }
            
            String reviewerName = r.getReviewer() != null ? r.getReviewer().getFullName() : "Khuyết danh";
            String initials = reviewerName.substring(0, Math.min(2, reviewerName.length())).toUpperCase();
            String reviewerAvatar = r.getReviewer() != null ? r.getReviewer().getAvatar() : null;

            Optional<ReviewReply> replyOpt = reviewReplyRepository.findByReviewId(r.getId());

            return ReviewData.builder()
                    .id(r.getId())
                    .reviewerName(reviewerName)
                    .reviewerInitials(initials)
                    .reviewerAvatar(reviewerAvatar)
                    .rating(r.getRating())
                    .comment(r.getComment())
                    .productName(pName)
                    .createdAt(r.getCreatedAt())
                    .replyContent(replyOpt.map(ReviewReply::getContent).orElse(null))
                    .replyCreatedAt(replyOpt.map(ReviewReply::getCreatedAt).orElse(null))
                    .build();
        }).toList();

        return SellerProfileResponse.builder()
                .id(seller.getId())
                .fullName(seller.getFullName())
                .email(seller.getEmail())
                .avatar(seller.getAvatar())
                .university(seller.getUniversity())
                .isVerified(seller.getIsVerified())
                .province(address != null ? address.getProvince() : "")
                .addressDetail(address != null ? address.getAddressDetail() : "")
                .activeListingCount(activeListings.size())
                .soldListingCount((int) soldCount)
                .activeListings(activeListings)
                .soldListings(soldListings)
                .averageRating(averageRating)
                .reviewCount(reviews.size())
                .reviews(reviewDataList)
                .build();
    }
}