package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreRes {

    private Long id;
    private String uid;
    private String name;
    private String address;
    private Vendor vendor;
    private String imgUrl;
    private Set<StoreEquipment> equipments;
    private Set<PoolTable> poolTables;
    private String lat;
    private String lon;
    private Integer deposit;
    private String hint;
    private String contactPhone;
    private Integer bookTime;
    private Integer cancelBookTime;
    private User user;
    private Set<StorePricingScheduleRes> pricingSchedules;
    // 當天的營業時間和價格
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    private Integer regularRate;
    private Integer discountRate;

    // 當天的時段列表
    private List<TimeSlotRes> timeSlots;

    // 特殊日期資訊（如果今天是特殊日期）
    private SpecialDateRes specialDateRes;

    private TodayRes todayRes;
}