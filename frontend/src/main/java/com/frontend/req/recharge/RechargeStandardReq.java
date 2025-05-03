package com.frontend.req.recharge;

import lombok.Data;

@Data
public class RechargeStandardReq {
    private Integer rechargeAmount;
    private Integer bonusAmount;
}