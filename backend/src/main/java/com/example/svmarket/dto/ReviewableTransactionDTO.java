package com.example.svmarket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewableTransactionDTO {
    private Integer orderId;
    private String transactionId;
    private String sellerName;
    private String sellerAvatar;
    private String productName;
    
    @JsonProperty("isReviewed")
    private Boolean isReviewed;
}