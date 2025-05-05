package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

/**
 * 當天店鋪資訊響應類
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TodayRes {
    // 是否為特殊日期
    private Boolean isSpecialDate;
    
    // 日期（如果是特殊日期）
    private String date;
    
    // 營業時間
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    
    // 關店時間
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    
    // 一般價格
    private Integer regularRate;
    
    // 優惠價格（如果不是特殊日期）
    private Integer discountRate;
    
    // 時段列表
    private List<TimeSlotRes> timeSlots;
}