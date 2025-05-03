package com.frontend.res.recharge;

import lombok.Data;

@Data
public class RechargePromotionDetailRes {
    private Long id;
    private Integer rechargeAmount;
    private Integer bonusAmount;
    private String status;
}