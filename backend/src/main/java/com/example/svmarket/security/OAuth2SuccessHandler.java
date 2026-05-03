package com.example.svmarket.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.svmarket.entity.Role;
import com.example.svmarket.entity.User;
import com.example.svmarket.repository.UserRepository;
import com.example.svmarket.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();

        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");
        String avatar = oauthUser.getAttribute("picture");

        // Kiểm tra bắt buộc email phải có đuôi .edu.vn
        if (email == null || !email.toLowerCase().endsWith(".edu.vn")) {
            response.sendRedirect("http://localhost:5174/login?error=edu_email_required");
            return;
        }

        // 1. check user
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setAvatar(avatar);
                    newUser.setRole(Role.USER);
                    newUser.setStatus("Đang hoạt động");
                    return userRepository.save(newUser);
                });

        // 2. generate JWT
        String token = jwtUtil.generateToken(user.getEmail());

        // 3. redirect ve frontend voi token tren URL
        response.sendRedirect(
                "http://localhost:5174/login?token=" + token
                + "&role=" + user.getRole()
        );
    }
}
