package com.example.svmarket.repository;

import java.util.List;
import java.util.Optional;

import com.example.svmarket.entity.PostSource;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.svmarket.entity.Listing;
import com.example.svmarket.entity.ListingStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ListingRepository extends JpaRepository<Listing, Integer> {

    List<Listing> findBySellerIdAndStatusNotOrderByCreatedAtDesc(Integer sellerId, ListingStatus status);

    List<Listing> findByStatusOrderByCreatedAtDesc(ListingStatus status);

    Optional<Listing> findByIdAndStatus(Integer id, ListingStatus status);

    Optional<Listing> findByIdAndSellerId(Integer id, Integer sellerId);

    List<Listing> findByStatus(ListingStatus status);

    /**
     * Tìm kiếm bài đăng theo trạng thái và từ khóa (title hoặc description chứa
     * keyword, không phân biệt hoa thường)
     */
    List<Listing> findByStatusAndTitleContainingIgnoreCaseOrStatusAndDescriptionContainingIgnoreCase(
            ListingStatus status1, String keyword1, ListingStatus status2, String keyword2);


    @Query("SELECT l FROM Listing l JOIN l.seller u WHERE l.status = :status AND LOWER(u.university) LIKE LOWER(CONCAT('%', :university, '%'))")
    List<Listing> findByUniversityCustom(@Param("status") ListingStatus status,
                                         @Param("university") String university);

    // loc bai dang theo truong
    @Query("""
    SELECT l FROM Listing l
    JOIN l.seller u
    LEFT JOIN SellerPackage sp ON sp.seller = u AND sp.status = 'ACTIVE'
    LEFT JOIN sp.packagePlan p
    WHERE l.status = 'ACTIVE'
    AND (:university IS NULL OR :university = '' 
         OR LOWER(u.university) LIKE LOWER(CONCAT('%', :university, '%')))
    """)
    List<Listing> findByUniversity(@Param("university") String university);

    // loc bai dang theo danh muc
    @Query("""
    SELECT l FROM Listing l
    JOIN l.category c
    LEFT JOIN SellerPackage sp ON sp.seller = l.seller AND sp.status = 'ACTIVE'
    LEFT JOIN sp.packagePlan p
    WHERE l.status = 'ACTIVE'
    AND c.id = :categoryId
    ORDER BY p.priorityLevel DESC, l.createdAt DESC
    """)
    List<Listing> findByCategoryId(@Param("categoryId") Integer categoryId);

    // Lọc tổng hợp: từ khóa, trường ĐH, danh mục, và sắp xếp linh hoạt
    @Query("""
    SELECT l FROM Listing l
    LEFT JOIN l.category c
    JOIN l.seller u
    LEFT JOIN SellerPackage sp ON sp.seller = u AND sp.status = 'ACTIVE'
    LEFT JOIN sp.packagePlan p
    WHERE l.status = 'ACTIVE'
    AND (:categoryId IS NULL OR c.id = :categoryId)
    AND (:university IS NULL OR :university = '' OR LOWER(u.university) LIKE LOWER(CONCAT('%', :university, '%')))
    AND (:keyword IS NULL OR :keyword = '' OR LOWER(l.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(l.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
    """)
    List<Listing> filterListingsCustom(
            @Param("keyword") String keyword,
            @Param("university") String university,
            @Param("categoryId") Integer categoryId,
            @Param("sortBy") String sortBy);

    List<Listing> findBySellerIdAndPostSource(Integer sellerId, PostSource postSource);
}
