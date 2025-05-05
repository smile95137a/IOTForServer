package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SpecialTimeSlotRes {

    private Long id;  // 特殊時段的 ID
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime startTime;  // 開始時間
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime endTime;  // 結束時間
    private Boolean isDiscount;  // 是否為折扣時段
    private Integer price;  // 該時段的價格

}
