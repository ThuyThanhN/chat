package com.example.svmarket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterOtpRequest {
    private String fullName;
    private String email;
    private String password;
    private String otp;
}

