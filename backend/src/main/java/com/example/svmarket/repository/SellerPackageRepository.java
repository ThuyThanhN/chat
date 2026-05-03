package com.example.svmarket.repository;

import com.example.svmarket.entity.SellerPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SellerPackageRepository extends JpaRepository<SellerPackage, Integer> {
    List<SellerPackage> findBySellerId(Integer sellerId);

    @Query("SELECT sp FROM SellerPackage sp " +
            "WHERE sp.seller.id = :userId " +
            "AND sp.status = 'ACTIVE' " +
            "AND sp.endDate > :now " +
            "AND sp.remainingPosts > 0 " +
            "ORDER BY sp.packagePlan.priorityLevel DESC")
    List<SellerPackage> findAvailablePackage(
            @Param("userId") Integer userId,
            @Param("now") java.time.LocalDateTime now
    );

    @Query("SELECT sp FROM SellerPackage sp JOIN FETCH sp.packagePlan " +
            "WHERE sp.seller.id = :sellerId " +
            "ORDER BY sp.startDate DESC")
    List<SellerPackage> findBySellerIdOrderByStartDateDesc(
            @Param("sellerId") Integer sellerId);

    // Chỉ cần gói còn hạn, không cần còn lượt đẩy
    @Query("SELECT sp FROM SellerPackage sp JOIN FETCH sp.packagePlan " +
            "WHERE sp.seller.id = :sellerId " +
            "AND sp.status = 'ACTIVE' " +
            "AND sp.endDate > :now " +
            "ORDER BY sp.packagePlan.priorityLevel DESC")
    Optional<SellerPackage> findActivePackage(
            @Param("sellerId") Integer sellerId,
            @Param("now") LocalDateTime now);

}
