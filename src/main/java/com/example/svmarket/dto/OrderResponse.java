package com.example.svmarket.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponse {
    private Integer id;
    private String buyerName;
    private String buyerInitials;
    private String product;
    private BigDecimal price;
    private String status;
    private String email;
    private LocalDateTime requestDate;
    private String note;
    private String imageUrl;
}