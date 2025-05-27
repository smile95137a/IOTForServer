package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public class TimeSlotInfo {
        private Boolean isDiscount;        // 是否为优惠时段
        private LocalTime startTime;       // 开始时间
        private LocalTime endTime;         // 结束时间
        private Double rate;               // 当前时段价格
        private Boolean isSpecialDate;     // 是否为特殊日期
        private String timeSlotType;       // 时段类型：DISCOUNT（优惠）/ REGULAR（一般）
    }