package com.example.svmarket.service;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.svmarket.dto.ForgotPasswordRequest;
import com.example.svmarket.dto.LoginRequest;
import com.example.svmarket.dto.LoginResponse;
import com.example.svmarket.dto.RegisterOtpRequest;
import com.example.svmarket.dto.RegisterRequest;
import com.example.svmarket.dto.ChangePasswordRequest;
import com.example.svmarket.dto.ResetPasswordRequest;
import com.example.svmarket.dto.VerifyPasswordOtpRequest;
import com.example.svmarket.entity.PasswordResetOTP;
import com.example.svmarket.entity.Role;
import com.example.svmarket.entity.User;
import com.example.svmarket.entity.Notification;
import com.example.svmarket.entity.NotificationType;
import com.example.svmarket.repository.NotificationRepository;
import com.example.svmarket.exception.BadRequestException;
import com.example.svmarket.repository.PasswordResetOTPRepository;
import com.example.svmarket.repository.UserRepository;
import com.example.svmarket.util.JwtUtil;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AuthService {
    private static final String PASSWORD_POLICY_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{6,}$";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordResetOTPRepository otpRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UniversityService universityService;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private NotificationRepository notificationRepository;

    // public LoginResponse login(LoginRequest req) {
    // System.out.println("INPUT: " + req.getEmailOrPhone());
    // System.out.println("PASS: " + req.getPassword());

    // // 1. Lấy thông tin user dưới database lên
    // User user = userRepository.findByEmailOrPhone(req.getEmailOrPhone());
    // System.out.println("USER = " + user);

    // if (user == null) {
    // return new LoginResponse(null, "Tài khoản không tồn tại");
    // }

    // // 2. So sánh mật khẩu đã mã hóa bằng BCrypt, vẫn giữ fallback cho dữ liệu cũ
    // // chưa encode.
    // if (!passwordEncoder.matches(req.getPassword(), user.getPassword())
    // && !user.getPassword().equals(req.getPassword())) {
    // return new LoginResponse(null, "Sai mật khẩu");
    // }
    // System.out.println("DB PASS = " + user.getPassword());
    // String token = jwtUtil.generateToken(user.getEmail());
    // System.out.println("REQ PASS=[" + req.getPassword() + "]");
    // System.out.println("LENGTH=" + req.getPassword().length());
    // return new LoginResponse(token, "Login success");
    // }

    public LoginResponse login(LoginRequest req) {
        String input = req.getEmailOrPhone() == null ? "" : req.getEmailOrPhone().trim();
        String password = req.getPassword() == null ? "" : req.getPassword();

        if (input.isEmpty() || password.isEmpty()) {
            return new LoginResponse(null, "Vui lòng nhập đầy đủ thông tin", null, null, null);
        }

        // 1. Tìm user
        User user = userRepository.findByEmailOrPhone(input);

        if (user == null) {
            return new LoginResponse(null, "Tài khoản không tồn tại", null, null, null);
        }

        if ("Đã khóa".equals(user.getStatus())) {
            return new LoginResponse(null, "Tài khoản của bạn đã bị khóa", null, null, null);
        }

        // 2. Kiểm tra mật khẩu 
        boolean isMatch = passwordEncoder.matches(password, user.getPassword())
                || user.getPassword().equals(password);

        if (!isMatch) {
            return new LoginResponse(null, "Sai mật khẩu", null, null, null);
        }

        // 3. Tạo token
        String token = jwtUtil.generateToken(user.getEmail());

        // 4. Trả thêm thông tin user
        return new LoginResponse(
                token,
                "Login success",
                user.getFullName(),
                user.getAvatar(),
                user.getRole() != null ? user.getRole().name() : "USER");
    }

    public void register(RegisterRequest registerRequest) {

        String email = registerRequest.getEmail().trim().toLowerCase();

        if (!email.endsWith(".edu.vn")) {
            throw new BadRequestException(
                    "Vui lòng sử dụng email sinh viên của trường (.edu.vn)");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email đã được đăng ký!");
        }

        if (!registerRequest.getPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new BadRequestException(
                    "Mật khẩu phải có chữ hoa, chữ thường, số, ký tự đặc biệt và tối thiểu 6 ký tự");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        otpRepository.deleteByEmail(registerRequest.getEmail());

        PasswordResetOTP otpEntity = new PasswordResetOTP();
        otpEntity.setEmail(registerRequest.getEmail());
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);
        emailService.sendOTP(registerRequest.getEmail(), otp, "dang ky tai khoan");
    }

    public void verifyRegistrationOTP(RegisterOtpRequest request, MultipartFile studentCard) {
        PasswordResetOTP otpEntity = otpRepository.findByEmailAndOtp(request.getEmail(), request.getOtp())
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được đăng ký!");
        }

        if (!request.getPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new BadRequestException(
                    "Mật khẩu phải có chữ hoa, chữ thường, số, ký tự đặc biệt và tối thiểu 6 ký tự");
        }

        if (studentCard == null || studentCard.isEmpty()) {
            throw new BadRequestException("Vui lòng upload ảnh thẻ sinh viên");
        }
        if (studentCard.getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("Ảnh không được vượt quá 5MB");
        }
        CloudinaryService.UploadedImage uploaded = cloudinaryService.uploadStudentCard(studentCard);
        String university = universityService.findUniversityByEmail(request.getEmail());

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus("Đang hoạt động");
        user.setRole(Role.USER);
        user.setUniversity(university); // lưu tên trường
        user.setIsVerified(false); // Chờ admin duyệt định danh
        user.setStudentCard(uploaded.secureUrl());

        userRepository.save(user);
        otpRepository.delete(otpEntity);

        List<User> admins = userRepository.findByRole(Role.ADMIN);
        for (User admin : admins) {
            Notification notification = Notification.builder()
                    .user(admin)
                    .content("Có yêu cầu duyệt định danh mới từ: " + user.getFullName())
                    .type(NotificationType.SYSTEM)
                    .referenceId(user.getId())
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        }
    }

    // Gửi OTP để đổi mật khẩu
    @Transactional
    public void requestPasswordResetOtp(ForgotPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();

        if (email.isEmpty()) {
            throw new BadRequestException("Vui lòng nhập email");
        }

        if (!userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email chưa được đăng ký");
        }

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Xóa OTP cũ của cùng email để tránh người dùng dùng nhầm mã cũ.
        otpRepository.deleteByEmail(email);

        PasswordResetOTP otpEntity = new PasswordResetOTP();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpEntity);

        try {
            emailService.sendOTP(email, otp, "quên mật khẩu");
        } catch (Exception e) {
            System.err.println("Lỗi gửi email OTP: " + e.getMessage());
            e.printStackTrace();
            throw new BadRequestException("Không thể gửi email OTP. Vui lòng kiểm tra cấu hình email server.");
        }
    }

    // Xác thực OTP quên mật khẩu trước khi cho phép nhập mật khẩu mới.
    public void verifyPasswordResetOtp(VerifyPasswordOtpRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String otp = request.getOtp() == null ? "" : request.getOtp().trim();

        if (email.isEmpty() || otp.isEmpty()) {
            throw new BadRequestException("Vui lòng nhập đầy đủ email và OTP");
        }

        if (!userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email chưa được đăng ký");
        }

        PasswordResetOTP otpEntity = otpRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }
    }

    // Xác nhận OTP + đổi mật khẩu mới
    public void resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim();
        String otp = request.getOtp() == null ? "" : request.getOtp().trim();
        String newPassword = request.getNewPassword() == null ? "" : request.getNewPassword();
        String confirmPassword = request.getConfirmPassword() == null ? "" : request.getConfirmPassword();

        if (email.isEmpty() || otp.isEmpty()) {
            throw new BadRequestException("Vui lòng nhập đầy đủ email và OTP");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new BadRequestException("Mật khẩu nhập lại không khớp");
        }

        if (!newPassword.matches(PASSWORD_POLICY_REGEX)) {
            throw new BadRequestException(
                    "Mật khẩu phải có chữ hoa, chữ thường, số, ký tự đặc biệt và tối thiểu 6 ký tự");
        }

        PasswordResetOTP otpEntity = otpRepository.findByEmailAndOtp(email, otp)
                .orElseThrow(() -> new BadRequestException("OTP không hợp lệ"));

        if (otpEntity.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP đã hết hạn");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Email chưa được đăng ký"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        otpRepository.delete(otpEntity);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("Tài khoản không tồn tại"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())
                && !user.getPassword().equals(request.getCurrentPassword())) {
            throw new BadRequestException("Mật khẩu hiện tại không đúng");
        }

        if (!request.getNewPassword().matches(PASSWORD_POLICY_REGEX)) {
            throw new BadRequestException("Mật khẩu phải có chữ hoa, chữ thường, số, ký tự đặc biệt và tối thiểu 6 ký tự");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}