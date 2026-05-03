package com.example.svmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDTO {
    private Integer id;
    private String transactionId;
    private String customerName;
    private BigDecimal amount;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
}