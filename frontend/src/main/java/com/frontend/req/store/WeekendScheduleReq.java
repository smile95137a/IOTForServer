package com.frontend.req.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeekendScheduleReq {
    
    // 是否啟用週末獨立設定
    private Boolean enableWeekendSetting = false;
    
    // 週末營業時間
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    
    // 週末價格設定
    private Double regularRate; // 週末普通時段價格
    private Double discountRate; // 週末優惠時段價格
    
    // 週末優惠時段
    private List<TimeSlotReq> timeSlots; // 週末優惠時段
}