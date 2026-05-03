package com.example.svmarket.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.svmarket.dto.NotificationResponse;
import com.example.svmarket.entity.Notification;
import com.example.svmarket.entity.User;
import com.example.svmarket.repository.NotificationRepository;
import com.example.svmarket.repository.UserRepository;

@Service
@Transactional
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public List<NotificationResponse> getMyNotifications(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(n -> NotificationResponse.builder()
                .id(n.getId()).content(n.getContent()).isRead(n.getIsRead()).type(n.getType() != null ? n.getType().name() : null).referenceId(n.getReferenceId()).createdAt(n.getCreatedAt()).build())
                .toList();
    }

    public void markAllAsRead(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        for (Notification n : notifications) {
            if (n.getIsRead() == null || !n.getIsRead()) {
                n.setIsRead(true);
            }
        }
        notificationRepository.saveAll(notifications);
    }

    public void markAsRead(String email, Integer notificationId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông báo"));

        if (notification.getUser().getId().equals(user.getId()) && (notification.getIsRead() == null || !notification.getIsRead())) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
}