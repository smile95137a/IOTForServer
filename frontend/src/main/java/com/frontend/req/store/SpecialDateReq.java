package com.frontend.req.store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpecialDateReq {
    private LocalDate date;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer regularRate;
    private Integer discountRate;
    private List<SpecialTimeSlotReq> timeSlots;
}
