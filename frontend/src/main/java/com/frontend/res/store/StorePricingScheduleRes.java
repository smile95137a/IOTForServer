package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
public class StorePricingScheduleRes {
    private String dayOfWeek; // 星期几
    private String regularStartTime; // 正常时段开始时间
    private String regularEndTime; // 正常时段结束时间
    private Integer regularRate; // 正常时段价格
    private String discountStartTime; // 优惠时段开始时间
    private String discountEndTime; // 优惠时段结束时间
    private Integer discountRate; // 优惠时段价格
}
