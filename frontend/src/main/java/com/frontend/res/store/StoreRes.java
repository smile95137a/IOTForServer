package com.frontend.res.store;

import com.frontend.entity.store.StorePricingSchedule;
import lombok.*;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class StoreRes {
    private Long id;
    private String uid;
    private String address;
    private String name;
    private Long availablesCount;
    private Long inusesCount;
    private String lat;
    private String lon;
    private Integer deposit;
    private String imgUrl;
    private List<StorePricingScheduleRes> pricingSchedules;

}

