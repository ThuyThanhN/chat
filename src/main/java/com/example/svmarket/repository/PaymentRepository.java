package com.example.svmarket.repository;

import com.example.svmarket.entity.Payment;
import com.example.svmarket.entity.PaymentStatus;
import com.example.svmarket.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByStatusOrderByCreatedAtDesc(PaymentStatus status);

    Optional<Payment> findTopByOrderIdOrderByIdDesc(Integer orderId);

    List<Payment> findByOrder_BuyerAndStatusOrderByIdDesc(User buyer, PaymentStatus status);
}