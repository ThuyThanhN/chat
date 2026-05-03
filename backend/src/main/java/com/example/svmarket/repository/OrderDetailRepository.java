package com.example.svmarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.svmarket.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}