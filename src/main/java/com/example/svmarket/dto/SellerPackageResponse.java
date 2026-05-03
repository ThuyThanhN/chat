package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class SellerPackageResponse {
    private Integer id;
    private String packageName;
    private Integer remainingPosts;
    private Integer remainingPushes;
    private Integer postLimit;   // thêm
    private Integer pushLimit;   // thêm
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
