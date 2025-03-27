package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePricingScheduleReq {
    private String dayOfWeek;
    private String regularStartTime;
    private String regularEndTime;
    private Integer regularRate;
    private String discountStartTime;
    private String discountEndTime;
    private Integer discountRate;
}
