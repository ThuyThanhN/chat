package com.example.svmarket.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Builder
public class BuyerReviewDTO {
    private Integer reviewId;
    private Integer orderId;
    private String transactionId;
    private String buyerName;
    private String buyerAvatar;
    private String productName;
    private Integer rating;
    private String comment;

    @JsonProperty("isReplied")
    private boolean isReplied;

    private LocalDateTime createdAt;
}