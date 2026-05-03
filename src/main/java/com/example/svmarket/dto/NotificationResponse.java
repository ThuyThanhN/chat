package com.example.svmarket.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private Integer id;
    private String content;
    private Boolean isRead;
    private String type;
    private Integer referenceId;
    private LocalDateTime createdAt;
}