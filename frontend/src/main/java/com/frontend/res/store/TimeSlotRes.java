package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString
@AllArgsConstructor
public class TimeSlotRes {
    private LocalTime startTime; // 开始时间
    private LocalTime endTime; // 结束时间
    private Boolean isDiscount; // 是否是优惠时段
}
