package com.example.svmarket.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderRequest {
    private Integer listingId;
    private String note;
    private String deliveryMethod;
}