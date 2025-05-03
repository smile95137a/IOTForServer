package com.frontend.req.recharge;

import lombok.Data;

@Data
public class RechargePromotionDetailReq {
    private Integer rechargeAmount;
    private Integer bonusAmount;
}