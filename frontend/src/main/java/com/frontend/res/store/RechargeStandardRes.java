package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.enums.PromotionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RechargeStandardRes {
    private Long id;
    private Integer rechargeAmount;
    private Integer bonusAmount;
    private PromotionStatus status;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
}
