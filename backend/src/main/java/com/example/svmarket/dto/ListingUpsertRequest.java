package com.example.svmarket.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ListingUpsertRequest {

    @NotBlank(message = "Tieu de bai dang khong duoc de trong")
    private String title;

    @NotNull(message = "Danh muc khong duoc de trong")
    private Integer categoryId;

    @NotNull(message = "Gia ban khong duoc de trong")
    @DecimalMin(value = "0", inclusive = false, message = "Gia ban phai lon hon 0")
    private BigDecimal price;

    private String deliveryAddress;

    private String conditionLevel;

    private String description;

    private String status;

    private String postSource;
}
