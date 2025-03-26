package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotReq {

    private LocalTime startTime; // 开始时间
    private LocalTime endTime; // 结束时间
    private Boolean isDiscount; // 是否是优惠时段
}
