package com.example.svmarket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyPasswordOtpRequest {
    private String email;
    private String otp;
}
