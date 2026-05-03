package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class PushHistoryResponse {
    private Integer listingId;
    private String listingTitle;
    private String packageName;
    private LocalDateTime lastPushAt;       // giờ bắt đầu đẩy
    private LocalDateTime pushExpiresAt;    // lastPushAt + pushHours
    private Integer remainingPushes;
    private boolean canPush;               // hết giờ hiệu lực + còn lượt
}
