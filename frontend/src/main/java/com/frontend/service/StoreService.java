package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.GlobalPricingOverride;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.TimeSlot;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.GlobalPricingOverrideRepository;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.AdminStoreRes;
import com.frontend.res.store.StorePricingScheduleRes;
import com.frontend.res.store.StoreRes;
import com.frontend.res.store.TimeSlotRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.frontend.entity.store.StorePricingSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private GlobalPricingOverrideRepository globalPricingOverrideRepository;

    // 获取所有商店的列表
    public List<StoreRes> findAll() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToAdminStoreRes)
                .collect(Collectors.toList());
    }

    // 将 Store 实体转换为 StoreRes
    private StoreRes convertToAdminStoreRes(Store store) {
        StoreRes.StoreResBuilder builder = StoreRes.builder()
                .id(store.getId())
                .uid(store.getUid())
                .name(store.getName())
                .address(store.getAddress())
                .imgUrl(store.getImgUrl())
                .lat(store.getLat())
                .lon(store.getLon())
                .deposit(store.getDeposit())
                .vendor(store.getVendor())
                .poolTables(store.getPoolTables()) // 如果 poolTables 不会导致循环引用，保持这个字段
                .hint(store.getHint())
                .contactPhone(store.getContactPhone())
                .bookTime(store.getBookTime())
                .cancelBookTime(store.getCancelBookTime());

        // 将 pricingSchedules 转换为 StorePricingScheduleRes
        if (store.getPricingSchedules() != null) {
            builder.pricingSchedules(store.getPricingSchedules().stream()
                    .map(this::convertToStorePricingScheduleRes) // 转换为 StorePricingScheduleRes
                    .collect(Collectors.toSet()));
        } else {
            builder.pricingSchedules(Set.of()); // 如果为 null，设为空集合
        }

        return builder.build();
    }

    // 将 StorePricingSchedule 转换为 StorePricingScheduleRes
    private StorePricingScheduleRes convertToStorePricingScheduleRes(StorePricingSchedule pricingSchedule) {
        // 将统一的时间段列表转换为普通时段和优惠时段
        List<TimeSlotRes> regularTimeSlots = pricingSchedule.getTimeSlots().stream()
                .filter(timeSlot -> !timeSlot.getIsDiscount()) // 筛选出普通时段
                .map(this::convertToTimeSlotRes) // 转换为 TimeSlotRes
                .collect(Collectors.toList());

        List<TimeSlotRes> discountTimeSlots = pricingSchedule.getTimeSlots().stream()
                .filter(timeSlot -> timeSlot.getIsDiscount()) // 筛选出优惠时段
                .map(this::convertToTimeSlotRes) // 转换为 TimeSlotRes
                .collect(Collectors.toList());

        return StorePricingScheduleRes.builder()
                .dayOfWeek(pricingSchedule.getDayOfWeek()) // 设置星期几
                .regularTimeSlots(regularTimeSlots) // 设置普通时段
                .discountTimeSlots(discountTimeSlots) // 设置优惠时段
                .regularRate(pricingSchedule.getRegularRate()) // 设置普通时段价格
                .discountRate(pricingSchedule.getDiscountRate()) // 设置优惠时段价格
                .openTime(pricingSchedule.getOpenTime())
                .closeTime(pricingSchedule.getCloseTime())
                .build();
    }

    // 将 TimeSlot 转换为 TimeSlotRes 的方法
    private TimeSlotRes convertToTimeSlotRes(TimeSlot timeSlot) {
        return new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getIsDiscount());
    }

    // 根据当前用户 UID 获取商店列表，并根据当前星期几返回定价时段
    public List<StoreRes> countAvailableAndInUseByUid(String uid) {
        // 获取当前星期几
        String currentDay = LocalDate.now().getDayOfWeek().toString();
        // 获取当前时间
        LocalTime currentTime = LocalTime.now();
        // 获取当前日期
        LocalDate currentDate = LocalDate.now();

        // 查询包含池台状态计数的商店列表
        List<Store> stores = storeRepository.findStoresWithPoolTableCountsByUid(uid);

        // 查询全局定价覆盖规则
        List<GlobalPricingOverride> globalPricingOverrides = globalPricingOverrideRepository.findAllByStartDateBeforeAndEndDateAfter(currentDate, currentDate);

        // 检查是否有有效的全局定价覆盖规则
        boolean hasValidOverride = !globalPricingOverrides.isEmpty();
        GlobalPricingOverride activeOverride = hasValidOverride ? globalPricingOverrides.get(0) : null;

        // 转换为 StoreRes 并过滤当前星期的定价时间表
        return stores.stream().map(store -> {
            // 转换为 StoreRes
            StoreRes storeRes = convertToAdminStoreRes(store);

            // 判断当前商店是否有匹配当天星期几的定价时间表
            Set<StorePricingScheduleRes> currentDayPricingSchedules = storeRes.getPricingSchedules().stream()
                    .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .collect(Collectors.toSet());

            // 如果存在有效的全局定价覆盖，应用全局定价规则
            if (hasValidOverride) {
                Set<StorePricingScheduleRes> overriddenSchedules = new HashSet<>();

                for (StorePricingScheduleRes schedule : currentDayPricingSchedules) {
                    // 创建新的定价时间表（复制原始时间表）
                    StorePricingScheduleRes overriddenSchedule = StorePricingScheduleRes.builder()
                            .dayOfWeek(schedule.getDayOfWeek())
                            .openTime(schedule.getOpenTime())
                            .closeTime(schedule.getCloseTime())
                            .build();

                    // 处理价格覆盖
                    // 默认使用全局覆盖价格
                    overriddenSchedule.setRegularRate(activeOverride.getRegularRate());
                    overriddenSchedule.setDiscountRate(activeOverride.getDiscountRate());

                    // 处理时间段覆盖
                    if (activeOverride.getTimeSlots() != null && !activeOverride.getTimeSlots().isEmpty()) {
                        // 创建新的时间段列表
                        List<TimeSlotRes> newRegularTimeSlots = new ArrayList<>();
                        List<TimeSlotRes> newDiscountTimeSlots = new ArrayList<>();

                        // 将全局时间段转换为TimeSlotRes
                        List<TimeSlotRes> globalTimeSlots = activeOverride.getTimeSlots().stream()
                                .map(globalTimeSlot -> new TimeSlotRes(
                                        globalTimeSlot.getStartTime(),
                                        globalTimeSlot.getEndTime(),
                                        globalTimeSlot.getIsDiscount()))
                                .collect(Collectors.toList());

                        // 分别处理常规时段和折扣时段
                        for (TimeSlotRes globalTimeSlot : globalTimeSlots) {
                            if (globalTimeSlot.getIsDiscount()) {
                                newDiscountTimeSlots.add(globalTimeSlot);
                            } else {
                                newRegularTimeSlots.add(globalTimeSlot);
                            }
                        }

                        // 如果全局覆盖没有指定时间段，则保留原有时间段
                        if (newRegularTimeSlots.isEmpty()) {
                            newRegularTimeSlots.addAll(schedule.getRegularTimeSlots());
                        }

                        if (newDiscountTimeSlots.isEmpty()) {
                            newDiscountTimeSlots.addAll(schedule.getDiscountTimeSlots());
                        }

                        // 设置新的时间段列表
                        overriddenSchedule.setRegularTimeSlots(newRegularTimeSlots);
                        overriddenSchedule.setDiscountTimeSlots(newDiscountTimeSlots);
                    } else {
                        // 如果没有全局时间段定义，保留原有时间段
                        overriddenSchedule.setRegularTimeSlots(schedule.getRegularTimeSlots());
                        overriddenSchedule.setDiscountTimeSlots(schedule.getDiscountTimeSlots());
                    }

                    overriddenSchedules.add(overriddenSchedule);
                }

                // 用覆盖后的时间表替换原有时间表
                if (!overriddenSchedules.isEmpty()) {
                    storeRes.setPricingSchedules(overriddenSchedules);
                }
            }

            return storeRes;
        }).collect(Collectors.toList());
    }
}