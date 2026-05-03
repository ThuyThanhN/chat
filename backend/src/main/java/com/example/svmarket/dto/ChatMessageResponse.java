package com.example.svmarket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatMessageResponse {
    private Integer id;
    private Integer conversationId;

    private Integer senderId;
    private String senderName;
    private String senderAvatar;

    private String content;
    private Boolean isRead;
    private Boolean isMine;
    private LocalDateTime createdAt;
}
