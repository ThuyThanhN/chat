package com.example.svmarket.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.svmarket.dto.ProfileResponse;
import com.example.svmarket.dto.UpdateProfileRequest;
import com.example.svmarket.dto.UserResponse;
import com.example.svmarket.repository.UserRepository;
import com.example.svmarket.service.UserService;
import com.example.svmarket.util.JwtUtil;

import com.example.svmarket.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ProfileResponse getProfile(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return userService.getProfile(email);
    }

    // UPDATE PROFILE
    @PutMapping("/profile")
    public String updateProfile(
            @RequestHeader("Authorization") String token,
            @RequestBody UpdateProfileRequest request) {
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        userService.updateProfile(email, request);

        return "Cập nhật thành công";
    }

    @PostMapping("/avatar")
    public String uploadAvatar(
            @RequestHeader("Authorization") String token,
            @RequestParam("file") MultipartFile file) {
        token = token.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);

        return userService.uploadAvatar(email, file);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser(HttpServletRequest request) {

        // Lấy token từ header
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Token không hợp lệ");
        }

        String token = authHeader.substring(7);

        // Lấy email từ token
        String email = jwtUtil.extractEmail(token);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return new UserResponse(
                user.getFullName(),
                user.getAvatar(),
                user.getGender() != null ? user.getGender().name() : "OTHER");
    }

    @GetMapping("/{id}/seller-profile")
    public ResponseEntity<?> getSellerProfile(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(userService.getSellerProfile(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}