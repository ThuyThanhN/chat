package com.example.svmarket.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 1;

    @Column(name = "condition_level", length = 100)
    private String conditionLevel;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(length = 50)
    private ListingStatus status = ListingStatus.INACTIVE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL)
    private List<Image> images;

    @OneToMany(mappedBy = "listing")
    private List<Review> reviews;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "last_push_at")
    private LocalDateTime lastPushAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_source", length = 20)
    private PostSource postSource; // người đăng bài chọn đăng free hay gói

    @ManyToOne
    @JoinColumn(name = "seller_package_id")
    private SellerPackage sellerPackage;
}