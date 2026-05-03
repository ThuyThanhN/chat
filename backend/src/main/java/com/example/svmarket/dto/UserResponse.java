package com.example.svmarket.dto;

public class UserResponse {
    private String fullName;
    private String avatar;
    private String gender; 

    public UserResponse(String fullName, String avatar, String gender) {
        this.fullName = fullName;
        this.avatar = avatar;
        this.gender = gender;
    }

    public String getFullName() {
        return fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public String getGender() {
        return gender;
    }
}