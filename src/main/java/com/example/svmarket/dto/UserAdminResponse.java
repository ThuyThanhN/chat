package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserAdminResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String university;
    private String avatar;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private Integer postCount;
    private Integer reportCount;
}