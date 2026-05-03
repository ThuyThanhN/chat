package com.example.svmarket.controller;

import com.example.svmarket.dto.UpdateUserAdminRequest;
import com.example.svmarket.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Integer id,
            @RequestBody UpdateUserAdminRequest request) {
        adminUserService.updateUser(id, request);
        return ResponseEntity.ok("Cập nhật người dùng thành công");
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<?> getPendingVerificationUsers() {
        return ResponseEntity.ok(adminUserService.getPendingVerificationUsers());
    }

    @PutMapping("/{id}/verify")
    public ResponseEntity<?> verifyStudent(@PathVariable Integer id) {
        adminUserService.verifyStudent(id);
        return ResponseEntity.ok("Duyệt định danh thành công");
    }

    @PutMapping("/{id}/reject-verification")
    public ResponseEntity<?> rejectVerification(@PathVariable Integer id) {
        adminUserService.rejectVerification(id);
        return ResponseEntity.ok("Từ chối định danh thành công");
    }
}