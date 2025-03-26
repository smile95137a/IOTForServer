package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePricingScheduleReq {

    private String dayOfWeek; // 星期几 (例如: MONDAY)
    private List<TimeSlotReq> regularTimeSlots; // 普通时段列表
    private List<TimeSlotReq> discountTimeSlots; // 优惠时段列表
    private Integer regularRate; // 普通时段价格
    private Integer discountRate; // 优惠时段价格
}
