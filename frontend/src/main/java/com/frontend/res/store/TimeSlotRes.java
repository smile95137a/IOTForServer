package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

import java.time.LocalTime;

@Data
@ToString
@AllArgsConstructor
public class TimeSlotRes {
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime; // 开始时间
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime; // 结束时间
    private Boolean isDiscount; // 是否是优惠时段
}
