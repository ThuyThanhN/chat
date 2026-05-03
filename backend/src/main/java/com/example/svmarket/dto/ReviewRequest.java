package com.example.svmarket.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Integer orderId;
    private Integer rating;
    private String comment;
}