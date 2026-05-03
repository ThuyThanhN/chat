package com.example.svmarket.repository;

import com.example.svmarket.entity.PasswordResetOTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetOTPRepository extends JpaRepository<PasswordResetOTP, Integer> {
    Optional<PasswordResetOTP> findByEmailAndOtp(String email, String otp);
    void deleteByEmail(String email);
}