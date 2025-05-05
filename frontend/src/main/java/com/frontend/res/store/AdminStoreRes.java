package com.frontend.res.store;

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

    // 從 PricingSchedule 獲取的基本資料
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer regularRate;
    private Integer discountRate;
    private List<TimeSlotRes> timeSlots;
    private Set<StorePricingScheduleRes> pricingSchedules;
    // 特殊日期
    private List<SpecialDateRes> specialDates;
}
