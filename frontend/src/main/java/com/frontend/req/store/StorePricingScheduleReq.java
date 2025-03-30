package com.frontend.req.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.controller.admin.StoreReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePricingScheduleReq {
    @JsonBackReference("storePricingSchedulesReference")
    private StoreReq store;
    private String dayOfWeek; // 星期几 (例如: MONDAY)
    private List<TimeSlotReq> timeSlots; // 所有时段列表 (包含普通时段和优惠时段)
    private Integer regularRate; // 普通时段价格
    private Integer discountRate; // 优惠时段价格
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
}
