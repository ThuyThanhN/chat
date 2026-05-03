package com.example.svmarket.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.svmarket.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
}