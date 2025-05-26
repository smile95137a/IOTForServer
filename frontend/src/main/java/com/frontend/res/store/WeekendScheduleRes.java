package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WeekendScheduleRes {
    private Boolean enableWeekendSetting; // 是否啟用週末獨立設定
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime; // 週末營業開始時間
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime; // 週末營業結束時間
    
    private Double regularRate; // 週末一般時段價格
    private Double discountRate; // 週末優惠時段價格
    private List<TimeSlotRes> timeSlots; // 週末優惠時段列表
}