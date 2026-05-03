package com.example.svmarket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatSendMessageRequest {
    @NotNull(message = "conversationId không được để trống")
    private Integer conversationId;

    @NotBlank(message = "Nội dung tin nhắn không được để trống")
    private String content;
}
