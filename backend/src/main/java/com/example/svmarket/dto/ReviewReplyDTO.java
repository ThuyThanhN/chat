package com.example.svmarket.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewReplyDTO {
    private String content;
    private LocalDateTime createdAt;
}