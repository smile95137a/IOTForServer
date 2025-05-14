package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialTimeSlotReq {
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isDiscount;
    private Double price;
}
