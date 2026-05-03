package com.example.svmarket.controller;

import com.example.svmarket.dto.ChatSendMessageRequest;
import com.example.svmarket.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@Controller
public class ChatSocketController {

    @Autowired
    private ChatService chatService;

    // Nhan tin nhan tu websocket client, luu DB va day realtime den 2 ben.
    @MessageMapping("/chat.send")
    public void sendMessage(@Valid @Payload ChatSendMessageRequest request, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bạn chưa đăng nhập");
        }

        chatService.sendMessage(principal.getName(), request);
    }
}
