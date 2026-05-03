package com.example.svmarket.dto;

import lombok.Data;

@Data
public class ReviewReplyRequest {
    private Integer reviewId;
    private String content;
}