package com.frontend.req.store;

import com.frontend.enums.PromotionStatus;
import lombok.Data;

@Data
public class RechargeStandardReq {
    private Integer rechargeAmount;
    private Integer bonusAmount;
    private PromotionStatus status;
}
