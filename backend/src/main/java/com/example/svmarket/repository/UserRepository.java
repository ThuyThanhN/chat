package com.example.svmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.svmarket.entity.User;
import com.example.svmarket.entity.Role;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Tìm user theo email hoặc số điện thoại
    @Query("SELECT u FROM User u WHERE u.email = :input OR u.phone = :input")
    User findByEmailOrPhone(@Param("input") String input);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    List<User> findByRole(Role role);

    // Lấy danh sách sinh viên chờ duyệt định danh
    @Query("SELECT u FROM User u WHERE (u.isVerified IS NULL OR u.isVerified = false) AND u.studentCard IS NOT NULL AND u.role = 'USER'")
    List<User> findPendingVerificationUsers();
}
