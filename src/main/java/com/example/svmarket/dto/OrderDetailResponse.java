package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class OrderDetailResponse {
    private Integer orderId;
    private String buyerName;
    private String sellerName;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private String paymentStatus;  // từ bảng payments
    private String paymentMethod;
    private LocalDateTime paidAt;
    private List<OrderItemResponse> items;
}
