package com.example.svmarket.controller;

import com.example.svmarket.dto.PaymentDTO;
import com.example.svmarket.entity.PaymentStatus;
import com.example.svmarket.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public List<PaymentDTO> getSuccessfulPayments() {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.SUCCESS)
                .stream()
                .map(payment -> {
                    PaymentDTO dto = new PaymentDTO();
                    dto.setId(payment.getId());
                    dto.setTransactionId(payment.getTransactionId());
                    dto.setCustomerName(payment.getOrder() != null && payment.getOrder().getBuyer() != null ? payment.getOrder().getBuyer().getFullName() : "Khách hàng");
                    dto.setAmount(payment.getAmount());
                    dto.setStatus("Đã thanh toán");
                    dto.setPaymentMethod(payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "VNPay");
                    dto.setCreatedAt(payment.getCreatedAt());
                    return dto;
                })
                .collect(Collectors.toList());
    }
}