package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@Builder
public class StorePricingScheduleRes {

    private String dayOfWeek;
    private List<TimeSlotRes> regularTimeSlots;
    private List<TimeSlotRes> discountTimeSlots;
    private Integer regularRate;
    private Integer discountRate;

}

