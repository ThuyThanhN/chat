package com.example.svmarket.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatStartConversationRequest {
    @NotNull(message = "listingId không được để trống")
    private Integer listingId;
}
