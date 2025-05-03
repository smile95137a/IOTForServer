package com.frontend.res.recharge;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RechargePromotionRes {
    private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<RechargePromotionDetailRes> details;
}