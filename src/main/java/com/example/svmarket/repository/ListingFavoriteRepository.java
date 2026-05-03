package com.example.svmarket.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.svmarket.entity.ListingFavorite;

public interface ListingFavoriteRepository extends JpaRepository<ListingFavorite, Integer> {
    Optional<ListingFavorite> findByUserIdAndListingId(Integer userId, Integer listingId);

    List<ListingFavorite> findByUserIdOrderByCreatedAtDesc(Integer userId);
}
