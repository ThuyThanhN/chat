package com.example.svmarket.controller;

import com.example.svmarket.dto.SellerPackageResponse;
import com.example.svmarket.service.SellerPackageService;
import com.example.svmarket.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/my-packages")
public class SellerPackageController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private SellerPackageService sellerPackageService;
    @GetMapping
    public List<SellerPackageResponse> getMyPackages(
            @RequestHeader("Authorization") String token) {

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        return sellerPackageService.getMyPackages(email);
    }
}
