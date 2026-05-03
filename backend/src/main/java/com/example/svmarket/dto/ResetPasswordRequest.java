package com.example.svmarket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {
    private String email;
    private String otp;
    private String newPassword;
    private String confirmPassword;
}
