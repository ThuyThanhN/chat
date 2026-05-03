package com.example.svmarket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.svmarket.dto.NotificationResponse;
import com.example.svmarket.service.NotificationService;
import com.example.svmarket.util.JwtUtil;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/my")
    public List<NotificationResponse> getMyNotifications(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            return java.util.Collections.emptyList();
        }
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            return notificationService.getMyNotifications(email);
        } catch (Exception e) {
            return java.util.Collections.emptyList();
        }
    }

    @PutMapping("/mark-read")
    public void markAllAsRead(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            return;
        }
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            notificationService.markAllAsRead(email);
        } catch (Exception e) {
            // Bỏ qua lỗi nếu token không hợp lệ
        }
    }

    @PutMapping("/{id}/read")
    public void markAsRead(@PathVariable Integer id, @RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ") || token.equals("Bearer null")) {
            return;
        }
        try {
            String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
            notificationService.markAsRead(email, id);
        } catch (Exception e) {
            // Bỏ qua lỗi nếu token không hợp lệ
        }
    }
}