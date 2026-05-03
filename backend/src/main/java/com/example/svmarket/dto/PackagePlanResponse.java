package com.example.svmarket.dto;

import java.math.BigDecimal;

import com.example.svmarket.entity.PackageStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PackagePlanResponse {
    Integer id;
    String name;
    BigDecimal price;
    Integer postLimit;
    Integer pushLimit;
    Integer pushHours;
    Integer durationDays;
    Integer priorityLevel;
    PackageStatus status;
}
