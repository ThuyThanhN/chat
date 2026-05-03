package com.example.svmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.example.svmarket.dto.ForgotPasswordRequest;
import com.example.svmarket.dto.LoginRequest;
import com.example.svmarket.dto.LoginResponse;
import com.example.svmarket.dto.RegisterOtpRequest;
import com.example.svmarket.dto.RegisterRequest;
import com.example.svmarket.dto.ResetPasswordRequest;
import com.example.svmarket.dto.VerifyPasswordOtpRequest;
import com.example.svmarket.service.AuthService;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    // Gửi OTP
    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        authService.register(request);
        return "OTP đã được gửi về email";
    }

    // Xác nhận OTP + tạo tài khoản
    @PostMapping(value = "/register/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String verifyOtp(
            @RequestPart("data") RegisterOtpRequest request,
            @RequestPart("studentCard") MultipartFile studentCard) {
        authService.verifyRegistrationOTP(request, studentCard);
        return "Đăng ký thành công";
    }

    // Gửi OTP để đổi mật khẩu
    @PostMapping("/password/otp")
    public String sendForgotPasswordOtp(@RequestBody ForgotPasswordRequest request) {
        authService.requestPasswordResetOtp(request);
        return "OTP đổi mật khẩu đã được gửi về email";
    }

    // Xác nhận OTP quên mật khẩu (bước riêng trước khi nhập mật khẩu mới)
    @PostMapping("/password/verify-otp")
    public String verifyForgotPasswordOtp(@RequestBody VerifyPasswordOtpRequest request) {
        authService.verifyPasswordResetOtp(request);
        return "OTP hợp lệ";
    }

    // Xác nhận OTP + đổi mật khẩu mới
    @PostMapping("/password/reset")
    public String resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return "Đổi mật khẩu thành công";
    }
}