package com.example.svmarket.repository;

import com.example.svmarket.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Integer> {
    List<Image> findByListingId(Integer listingId);

    void deleteByListingId(Integer listingId);
}
