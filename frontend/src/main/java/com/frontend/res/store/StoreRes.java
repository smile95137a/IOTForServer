package com.frontend.res.store;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

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
    private Integer regularRate;      // 新增字段
    private Integer discountRate;     // 新增字段
    private String regularTimeRange;  // 新增字段
}

