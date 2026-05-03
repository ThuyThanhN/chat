package com.example.svmarket.repository;

import com.example.svmarket.entity.PackagePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.svmarket.entity.PackageStatus;


public interface PackagePlanRepository extends JpaRepository<PackagePlan, Integer> {
    //search va filter goi tin
    @Query("""
    SELECT p FROM PackagePlan p
    WHERE (:name IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')))
    AND (:status IS NULL OR p.status = :status)
    AND (:minPost IS NULL OR p.postLimit >= :minPost)
    AND (:maxPost IS NULL OR p.postLimit <= :maxPost)
""")
    Page<PackagePlan> search(
            @Param("name") String name,
            @Param("status") PackageStatus status,
            @Param("minPost") Integer minPost,
            @Param("maxPost") Integer maxPost,
            Pageable pageable
    );

}
