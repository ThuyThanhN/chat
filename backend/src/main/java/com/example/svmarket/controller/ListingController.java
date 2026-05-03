package com.example.svmarket.controller;

import java.util.List;
import java.util.Map;

import com.example.svmarket.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.svmarket.repository.ListingRepository;
import com.example.svmarket.service.ListingService;
import com.example.svmarket.util.JwtUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/listings")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class ListingController {

    @Autowired
    private ListingService listingService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    ListingRepository listingRepository;

    // Lay danh sach danh muc de frontend do vao dropdown.
    @GetMapping("/categories")
    public List<CategoryOptionResponse> getCategories() {
        return listingService.getCategories();
    }

    // Lọc tổng hợp: từ khóa, trường đại học, danh mục và sắp xếp giá
    @GetMapping
    public List<ListingSummaryResponse> filterListings(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "university", required = false) String university,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "sortBy", required = false) String sortBy) {
        return listingService.filterListingsCustom(keyword, university, categoryId, sortBy);
    }

    // Ho tro route cu /active cho frontend.
    @GetMapping("/active")
    public List<ListingSummaryResponse> getActiveListingsLegacyRoute() {
        return listingService.getActiveListings();
    }

    // Lọc bài đăng theo trường đại học
    @GetMapping("/university")
    public List<ListingSummaryResponse> filterListingsByUniversity(@RequestParam("name") String name) {
        return listingService.filterByUniversity(name);
    }

    // Them/bo luu bai dang theo user dang nhap.
    @PostMapping("/{id}/favorite")
    public FavoriteToggleResponse toggleFavoriteListing(@RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        String email = extractEmail(token);
        return listingService.toggleFavoriteListing(email, id);
    }

    // Lay danh sach bai dang da luu cua user dang nhap.
    @GetMapping("/favorites/my")
    public List<ListingSummaryResponse> getMyFavoriteListings(@RequestHeader("Authorization") String token) {
        String email = extractEmail(token);
        return listingService.getMyFavoriteListings(email);
    }

    // Lay danh sach id bai dang da luu cua user dang nhap.
    @GetMapping("/favorites/my/ids")
    public List<Integer> getMyFavoriteListingIds(@RequestHeader("Authorization") String token) {
        String email = extractEmail(token);
        return listingService.getMyFavoriteListingIds(email);
    }

    // Lay chi tiet bai dang dang hoat dong cho trang chi tiet san pham.
    @GetMapping("/{id:\\d+}")
    public ListingDetailResponse getActiveListingById(@PathVariable Integer id) {
        return listingService.getActiveListingById(id);
    }

    // Tao bai dang moi cua nguoi dang nhap.
    @PostMapping(value = "/my", consumes = { "multipart/form-data" })
    @ResponseStatus(HttpStatus.CREATED)
    public ListingDetailResponse createMyListing(@RequestHeader("Authorization") String token,
            @Valid @ModelAttribute ListingUpsertRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        String email = extractEmail(token);
        return listingService.createMyListing(email, request, images);
    }

    // Lay danh sach bai dang cua nguoi dang nhap.
    @GetMapping("/my")
    public List<ListingSummaryResponse> getMyListings(@RequestHeader("Authorization") String token) {
        String email = extractEmail(token);
        return listingService.getMyListings(email);
    }

    // Lay chi tiet 1 bai dang de xem/sua.
    @GetMapping("/my/{id}")
    public ListingDetailResponse getMyListingById(@RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        String email = extractEmail(token);
        return listingService.getMyListingById(email, id);
    }

    // Cap nhat bai dang cua nguoi dang nhap.
    @PutMapping(value = "/my/{id}", consumes = { "multipart/form-data" })
    public ListingDetailResponse updateMyListing(@RequestHeader("Authorization") String token,
            @PathVariable Integer id,
            @Valid @ModelAttribute ListingUpsertRequest request,
            @RequestParam(value = "images", required = false) List<MultipartFile> images) {
        String email = extractEmail(token);
        return listingService.updateMyListing(email, id, request, images);
    }

    // Xoa mem bai dang cua nguoi dang nhap.
    @DeleteMapping("/my/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMyListing(@RequestHeader("Authorization") String token,
            @PathVariable Integer id) {
        String email = extractEmail(token);
        listingService.deleteMyListing(email, id);
    }

    private String extractEmail(String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        return jwtUtil.extractEmail(token);
    }

    @GetMapping("/post-limit")
    public ResponseEntity<?> getPostLimit(@AuthenticationPrincipal String email) {
        return ResponseEntity.ok(listingService.getPostLimit(email));
    }

    @GetMapping("/featured")
    public List<ListingSummaryResponse> getFeaturedListings() {
        return listingService.getFeaturedListings();
    }

    // loc bai dang theo truong
    @GetMapping("/by-university")
    public List<ListingSummaryResponse> getByUniversity(
            @RequestParam String university) {
        return listingService.getListingsByUniversity(university);
    }

    // loc bai dang theo danh muc
    @GetMapping("/by-category")
    public List<ListingSummaryResponse> getByCategory(
            @RequestParam Integer categoryId) {
        return listingService.getListingsByCategory(categoryId);
    }

    // Lấy danh sách bài đăng (có mua gói) đã được duyệt
    @GetMapping("/history")
    public ResponseEntity<List<PushHistoryResponse>> getPushHistory(
            @RequestHeader("Authorization") String token) {

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        return ResponseEntity.ok(listingService.getPushHistory(email));
    }

    //  Xử lý đẩy lại bài đăng khi bấm nút "Đẩy lại"
    @PostMapping("/{id}/push")
    public ResponseEntity<?> pushListing(
            @PathVariable Integer id,
            @RequestHeader("Authorization") String token) {

        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        listingService.pushListing(email, id);

        return ResponseEntity.ok(Map.of("message", "Đẩy bài thành công"));
    }
}
