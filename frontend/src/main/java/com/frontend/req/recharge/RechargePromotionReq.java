package com.frontend.req.recharge;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RechargePromotionReq {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<RechargePromotionDetailReq> details;
}
