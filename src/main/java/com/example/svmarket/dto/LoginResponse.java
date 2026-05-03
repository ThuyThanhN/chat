package com.example.svmarket.dto;

public class LoginResponse {
    private String token;
    private String message;
    private String fullName;
    private String avatar;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, String message, String fullName, String avatar, String role) {
        this.token = token;
        this.message = message;
        this.fullName = fullName;
        this.avatar = avatar;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}