package com.example.svmarket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ListingDetailResponse {
    private Integer id;
    private String title;
    private Integer categoryId;
    private String categoryName;
    private BigDecimal price;
    private String deliveryAddress;
    private String conditionLevel;
    private String description;
    private String status;
    private List<String> imageUrls;
    private String sellerName;
    private String sellerUniversity;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private String rejectReason;
    private Integer sellerId;
    private String sellerAvatar;
    private String postSource;
    private Boolean isVerified;
}
