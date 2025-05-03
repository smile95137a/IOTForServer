package com.frontend.dto;

import java.time.LocalDate;

public class RechargePromotionDto {
    private Long id;
    private Integer rechargeAmount;
    private Integer bonusAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
}