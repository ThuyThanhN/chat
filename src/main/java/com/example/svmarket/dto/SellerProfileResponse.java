package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProfileResponse {
    private Integer id;
    private String fullName;
    private String email;
    private String avatar;
    private String university;
    private String province;
    private String addressDetail;
    private Integer activeListingCount;
    private Integer soldListingCount;
    private List<ListingSummaryResponse> activeListings;
    private List<ListingSummaryResponse> soldListings;
    private Double averageRating;
    private Integer reviewCount;
    private List<ReviewData> reviews;
    private Boolean isVerified;
}