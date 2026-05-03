package com.example.svmarket.controller;

import com.example.svmarket.dto.ChatConversationResponse;
import com.example.svmarket.dto.ChatMessageResponse;
import com.example.svmarket.dto.ChatStartConversationRequest;
import com.example.svmarket.service.ChatService;
import com.example.svmarket.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private JwtUtil jwtUtil;

    // Tao hoi thoai tu trang chi tiet san pham.
    @PostMapping("/conversations/start")
    public ChatConversationResponse startConversation(
            @RequestHeader("Authorization") String bearerToken,
            @Valid @RequestBody ChatStartConversationRequest request) {
        String email = extractEmail(bearerToken);
        return chatService.startConversation(email, request.getListingId());
    }

    // Lay danh sach hoi thoai cua nguoi dung.
    @GetMapping("/conversations")
    public List<ChatConversationResponse> getMyConversations(
            @RequestHeader("Authorization") String bearerToken) {
        String email = extractEmail(bearerToken);
        return chatService.getMyConversations(email);
    }

    // Lay toan bo tin nhan theo hoi thoai.
    @GetMapping("/conversations/{conversationId}/messages")
    public List<ChatMessageResponse> getConversationMessages(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer conversationId) {
        String email = extractEmail(bearerToken);
        return chatService.getConversationMessages(email, conversationId);
    }

    // Danh dau tin nhan la da doc.
    @PostMapping("/conversations/{conversationId}/read")
    public void markConversationRead(
            @RequestHeader("Authorization") String bearerToken,
            @PathVariable Integer conversationId) {
        String email = extractEmail(bearerToken);
        chatService.markConversationAsRead(email, conversationId);
    }

    private String extractEmail(String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token không hợp lệ");
        }
        return jwtUtil.extractEmail(bearerToken.replace("Bearer ", ""));
    }
}
