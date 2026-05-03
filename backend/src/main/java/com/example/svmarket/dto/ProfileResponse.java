package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Integer id;
    private String email;
    private String fullName;
    private String avatar;
    private String university;
    private String province;
    private String addressDetail;
    private String gender;
    private Boolean isVerified;
}