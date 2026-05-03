package com.example.svmarket.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "package_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackagePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(length = 150, nullable = false)
    private String name;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    // Số bài đăng
    @Column(name = "post_limit", nullable = false)
    private Integer postLimit;

    // Số lượt đẩy tin
    @Column(name = "push_limit", nullable = false)
    private Integer pushLimit;

    // Hiệu lực mỗi lần đẩy
    @Column(name = "push_hours", nullable = false)
    private Integer pushHours;

    // Thời hạn gói
    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    // Mức độ ưu tiên sort (1 = thấp, 2 = trung, 3 = cao nhất)
    @Column(name = "priority_level", nullable = false)
    private Integer priorityLevel;
    
    //Có vào mục đề xuất không
    @Column(name = "is_featured")
    private Boolean isFeatured;

    // Trạng thái gói
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    @Builder.Default
    private PackageStatus status = PackageStatus.ACTIVE;
}