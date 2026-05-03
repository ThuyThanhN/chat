package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ListingSummaryResponse {
    private Integer id;
    private String title;
    private BigDecimal price;
    private String status;
    private String thumbnailUrl;
    private String sellerUniversity;
    private String sellerName;
    private LocalDateTime createdAt;
    private Integer priorityLevel;
    private Boolean isFeatured;
    private boolean pushing;
    private Boolean isVerified;
}
