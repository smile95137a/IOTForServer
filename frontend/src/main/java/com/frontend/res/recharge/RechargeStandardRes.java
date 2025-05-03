package com.frontend.res.recharge;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RechargeStandardRes {
    private Long id;
    private Integer rechargeAmount;
    private Integer bonusAmount;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}