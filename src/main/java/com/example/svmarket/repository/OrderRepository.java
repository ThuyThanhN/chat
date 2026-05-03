package com.example.svmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.svmarket.entity.Order;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findBySellerIdOrderByCreatedAtDesc(Integer sellerId);

    List<Order> findByBuyerIdOrderByCreatedAtDesc(Integer buyerId);
}