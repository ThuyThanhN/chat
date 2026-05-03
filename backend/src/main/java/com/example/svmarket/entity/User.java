package com.example.svmarket.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    private String email;

    private String password;

    private String phone;

    private String avatar;

    private String university;

    private String status;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    // @Enumerated(EnumType.STRING)
    // private Role role;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder.Default
    @Column(name = "free_posts_remaining")
    private Integer freePostsRemaining = 3; // 3 lần đăng bài (mặc định)

    @Column(name = "free_reset_date")
    private LocalDateTime freeResetDate;

    @Column(name = "student_card")
    private String studentCard; // ảnh thẻ sinh viên

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses;

    @OneToMany(mappedBy = "seller")
    private List<Listing> listings;

    @OneToMany(mappedBy = "buyer")
    private List<Order> orders;

    @OneToMany(mappedBy = "user")
    private List<Notification> notifications;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        if (freePostsRemaining == null) {
            freePostsRemaining = 3;
        }

        if (freeResetDate == null) {
            freeResetDate = now.plusMonths(1);
        }

        if (createdAt == null) {
            createdAt = now;
        }
    }
}
