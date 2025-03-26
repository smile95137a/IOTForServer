package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePricingScheduleReq {

    private String dayOfWeek; // 星期几 (例如: MONDAY)
    private String regularStartTime; // 普通时段开始时间
    private String regularEndTime; // 普通时段结束时间
    private Integer regularRate; // 普通时段价格
    private String discountStartTime; // 优惠时段开始时间
    private String discountEndTime; // 优惠时段结束时间
    private Integer discountRate; // 优惠时段价格
}
