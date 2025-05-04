package com.frontend.req.store;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
public class GlobalPricingOverrideReq {
    private String name;
    private LocalDate startDate;  // 活动开始日期
    private LocalDate endDate;    // 活动结束日期
    private Integer regularRate;
    private Integer discountRate;
    private Long storeId;         // 添加店铺ID字段，用于查找店铺的营业时间
    private List<TimeSlotReq> timeSlots;

    @Data
    public static class TimeSlotReq {
        private LocalTime startTime;
        private LocalTime endTime;
        private Boolean isDiscount;  // 标识该时段是否是优惠时段
    }
}
