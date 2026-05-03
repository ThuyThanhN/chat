package com.example.svmarket.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String mailFrom;

    public void sendOTP(String toEmail, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setFrom(mailFrom);
        message.setSubject("Mã OTP " + purpose);
        message.setText("Mã OTP " + purpose.toLowerCase() + " của bạn là: " + otp + ". OTP có hiệu lực trong 5 phút.");
        mailSender.send(message);
    }

    public void sendOTP(String toEmail, String otp) {
        sendOTP(toEmail, otp, "Quên mật khẩu");
    }
}
