package com.frontend.res.store;

import com.frontend.entity.store.StorePricingSchedule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class StoreRes {
    private Long storeId;
    private String uid;
    private String address;
    private String name;
    private Long availablesCount;
    private Long inusesCount;
    private Long lat;
    private Long lon;
    private Integer deposit;
    private List<StorePricingScheduleRes> pricingSchedules;

}

