package com.frontend.res.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.res.vendor.VendorDto;
import jakarta.persistence.Column;
import lombok.*;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStoreRes {
    private Long id;
    private String uid;
    private String name;
    private String address;
    private VendorDto vendor;
    private String imgUrl;
    private Set<PoolTable> poolTables;
    private String lat;
    private String lon;
    private Integer deposit;
    private String hint;
    private String contactPhone;
    private Integer bookTime;
    private Integer cancelBookTime;
    private User user;

    // 平日設定 (週一到週五) - 從週一的 PricingSchedule 獲取
    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;
    private Double regularRate;
    private Double discountRate;
    private List<TimeSlotRes> timeSlots; // 平日優惠時段

    // 週末設定 (週六、週日共用) - 如果有啟用的話
    private WeekendScheduleRes weekendSchedule;

    // 完整的 pricing schedules（包含所有天的詳細資料）
    private Set<StorePricingScheduleRes> pricingSchedules;

    // 特殊日期設定
    private List<SpecialDateRes> specialDates;
}