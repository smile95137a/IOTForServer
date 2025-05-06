package com.frontend.res.recharge;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class RechargePromotionRes {
    private Long id;
    private String name;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDate startDate;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDate endDate;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<RechargePromotionDetailRes> details;
}