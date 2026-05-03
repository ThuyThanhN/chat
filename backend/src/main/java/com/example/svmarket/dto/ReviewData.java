package com.example.svmarket.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewData {
    private Integer id;
    private String reviewerName;
    private String reviewerInitials;
    private String reviewerAvatar;
    private Integer rating;
    private String comment;
    private String productName;
    private LocalDateTime createdAt;
    private String replyContent;
    private LocalDateTime replyCreatedAt;
}