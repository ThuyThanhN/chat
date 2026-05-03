package com.example.svmarket.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class ChatConversationResponse {
    private Integer conversationId;

    private Integer listingId;
    private String listingTitle;
    private String listingThumbnail;
    private BigDecimal listingPrice;

    private Integer partnerId;
    private String partnerName;
    private String partnerAvatar;
    private String partnerUniversity;

    private String lastMessage;
    private LocalDateTime updatedAt;
    private Long unreadCount;
}
