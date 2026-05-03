package com.example.svmarket.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.svmarket.dto.ListingDetailResponse;
import com.example.svmarket.dto.ListingSummaryResponse;
import com.example.svmarket.service.AdminListingService;

@RestController
@RequestMapping("/api/admin/listings")
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" })
public class AdminListingController {

    @Autowired
    private AdminListingService adminListingService;

    @GetMapping
    public List<ListingSummaryResponse> getAllListings() {
        return adminListingService.getAllListings();
    }

    @GetMapping("/pending")
    public List<ListingDetailResponse> getPendingListings() {
        return adminListingService.getPendingListings();
    }

    @GetMapping("/rejected")
    public List<ListingDetailResponse> getRejectedListings() {
        return adminListingService.getRejectedListings();
    }

    @PutMapping("/{id}/approve")
    public void approveListing(@PathVariable Integer id) {
        adminListingService.approveListing(id);
    }

    @PutMapping("/{id}/reject")
    public void rejectListing(@PathVariable Integer id, @RequestBody(required = false) java.util.Map<String, String> payload) {
        String reason = payload != null ? payload.get("reason") : null;
        adminListingService.rejectListing(id, reason);
    }
}