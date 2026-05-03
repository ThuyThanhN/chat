package com.example.svmarket.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String university;
    private String province;
    private String addressDetail;
    private String gender;
}