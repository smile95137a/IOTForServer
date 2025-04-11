package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;
import java.util.List;

@Data
@ToString
@AllArgsConstructor
@Builder
public class StorePricingScheduleRes {

    private String dayOfWeek;            // 星期几
    private List<TimeSlotRes> regularTimeSlots; // 普通时段
    private List<TimeSlotRes> discountTimeSlots; // 优惠时段
    private Integer regularRate;         // 普通时段价格
    private Integer discountRate;        // 优惠时段价格
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
}
