package com.example.svmarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_packages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "package_plan_id", nullable = false)
    private PackagePlan packagePlan;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private PackageStatus status = PackageStatus.ACTIVE;

    // Lượt bài đăng còn lại
    @Column(name = "remaining_posts", nullable = false)
    private Integer remainingPosts;

    // Lượt đẩy tin còn lại
    @Column(name = "remaining_pushes", nullable = false)
    private Integer remainingPushes;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @PrePersist
    protected void onCreate() {
        this.startDate = LocalDateTime.now();
    }
}