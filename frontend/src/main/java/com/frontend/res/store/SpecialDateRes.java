package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialDateRes {

    private Long id;  // 特殊日期 ID
    private String date;  // 特殊日期（可以使用 String 格式化 LocalDate）
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;  // 開放時間
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;  // 關閉時間
    private Integer regularRate;  // 正常時段的費用（如果有）
    private List<SpecialTimeSlotRes> timeSlots;  // 特殊時段列表

}
