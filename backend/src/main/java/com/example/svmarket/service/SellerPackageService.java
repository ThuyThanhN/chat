package com.example.svmarket.service;

import com.example.svmarket.dto.SellerPackageResponse;
import com.example.svmarket.entity.User;
import com.example.svmarket.repository.SellerPackageRepository;
import com.example.svmarket.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerPackageService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerPackageRepository sellerPackageRepository;

    public List<SellerPackageResponse> getMyPackages(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return sellerPackageRepository
                .findAvailablePackage(user.getId(), LocalDateTime.now())
                .stream()
                .map(pkg -> new SellerPackageResponse(
                        pkg.getId(),
                        pkg.getPackagePlan().getName(),
                        pkg.getRemainingPosts(),
                        pkg.getRemainingPushes(),
                        pkg.getPackagePlan().getPostLimit(),  // thêm
                        pkg.getPackagePlan().getPushLimit(),  // thêm
                        pkg.getStartDate(),
                        pkg.getEndDate()
                ))
                .toList();
    }
}
