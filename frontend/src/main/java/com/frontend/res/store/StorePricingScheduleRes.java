package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@Builder
public class StorePricingScheduleRes {

    private String dayOfWeek;            // 星期几
    private List<TimeSlotRes> regularTimeSlots;   // 普通时段列表
    private List<TimeSlotRes> discountTimeSlots;  // 优惠时段列表
    private Integer regularRate;         // 普通时段价格
    private Integer discountRate;        // 优惠时段价格

}
