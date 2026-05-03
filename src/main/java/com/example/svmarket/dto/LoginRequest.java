package com.example.svmarket.dto;

public class LoginRequest {
    private String emailOrPhone;
    private String password;
    public String getEmailOrPhone() {
        return emailOrPhone;
    }
    public void setEmailOrPhone(String emailOrPhone) {
        this.emailOrPhone = emailOrPhone;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

}