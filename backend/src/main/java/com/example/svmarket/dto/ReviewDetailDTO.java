package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDetailDTO {
    private String sellerAvatar;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
    private String replyContent;
    private LocalDateTime replyCreatedAt;
}