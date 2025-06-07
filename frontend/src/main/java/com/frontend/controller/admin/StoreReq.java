package com.frontend.controller.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.req.store.SpecialDateReq;
import com.frontend.req.store.StorePricingScheduleReq;
import com.frontend.req.store.TimeSlotReq;
import com.frontend.req.store.WeekendScheduleReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreReq {

    private String name; // 店铺名称
    private String address; // 店铺地址
    private Vendor vendor; // 店铺所属供应商
    private Set<PoolTable> poolTables; // 店铺池桌
    private Set<StoreEquipment> equipments; // 店铺设备
    private String lat; // 店铺纬度
    private String lon; // 店铺经度
    private Double regularRate; // 普通时段价格
    private Double discountRate; // 优惠时段价格
    private Integer deposit; // 店铺押金
    private String imgUrl; // 店铺图片 URL
    @JsonManagedReference("storePricingSchedulesReference")
    private Set<StorePricingScheduleReq> pricingSchedules;
    private String hint; // 提示信息
    private String contactPhone; // 联系电话
    private Integer bookTime;
    private Integer cancelBookTime;
    private User user;
    private String storeIP;
    // 平日設定 (週一到週五)
    private LocalTime openTime;
    private LocalTime closeTime;
    private List<TimeSlotReq> timeSlots; // 平日優惠區間

    // 週末設定 (週六、週日共用)
    private WeekendScheduleReq weekendSchedule; // 週末時段設定

    // 特殊日期設定
    private List<SpecialDateReq> specialDates; // 特殊日期與優惠時段
}
