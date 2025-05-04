package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.TimeSlot;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.AdminStoreRes;
import com.frontend.res.store.StorePricingScheduleRes;
import com.frontend.res.store.StoreRes;
import com.frontend.res.store.TimeSlotRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.frontend.entity.store.StorePricingSchedule;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // 获取所有商店的列表
    public List<StoreRes> findAll() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(store -> {
                    StoreRes storeRes = convertToAdminStoreRes(store);
                    storeRes.setPricingSchedules(adjustPricingSchedulesByToday(storeRes.getPricingSchedules()));
                    return storeRes;
                })
                .collect(Collectors.toList());
    }

    // 根据 ID 获取单个商店并应用今日活动定价逻辑
    public StoreRes findById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Store not found with id: " + id));
        StoreRes storeRes = convertToAdminStoreRes(store);
        storeRes.setPricingSchedules(adjustPricingSchedulesByToday(storeRes.getPricingSchedules()));
        return storeRes;
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
                .poolTables(store.getPoolTables())
                .hint(store.getHint())
                .contactPhone(store.getContactPhone())
                .bookTime(store.getBookTime())
                .cancelBookTime(store.getCancelBookTime());

        if (store.getPricingSchedules() != null) {
            builder.pricingSchedules(store.getPricingSchedules().stream()
                    .map(this::convertToStorePricingScheduleRes)
                    .collect(Collectors.toSet()));
        } else {
            builder.pricingSchedules(Set.of());
        }

        return builder.build();
    }

    // 将 StorePricingSchedule 转换为 StorePricingScheduleRes
    private StorePricingScheduleRes convertToStorePricingScheduleRes(StorePricingSchedule pricingSchedule) {
        List<TimeSlotRes> regularTimeSlots = pricingSchedule.getTimeSlots().stream()
                .filter(timeSlot -> !timeSlot.getIsDiscount())
                .map(this::convertToTimeSlotRes)
                .collect(Collectors.toList());

        List<TimeSlotRes> discountTimeSlots = pricingSchedule.getTimeSlots().stream()
                .filter(TimeSlot::getIsDiscount)
                .map(this::convertToTimeSlotRes)
                .collect(Collectors.toList());

        return StorePricingScheduleRes.builder()
                .dayOfWeek(pricingSchedule.getDayOfWeek())
                .regularTimeSlots(regularTimeSlots)
                .discountTimeSlots(discountTimeSlots)
                .regularRate(pricingSchedule.getRegularRate())
                .discountRate(pricingSchedule.getDiscountRate())
                .openTime(pricingSchedule.getOpenTime())
                .closeTime(pricingSchedule.getCloseTime())
                .build();
    }

    // 将 TimeSlot 转换为 TimeSlotRes
    private TimeSlotRes convertToTimeSlotRes(TimeSlot timeSlot) {
        return new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), timeSlot.getIsDiscount());
    }

    // 公用：根据当前星期几调整 pricingSchedules（优惠或常规）
    private Set<StorePricingScheduleRes> adjustPricingSchedulesByToday(Set<StorePricingScheduleRes> schedules) {
        String currentDay = LocalDate.now().getDayOfWeek().toString();

        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                .map(schedule -> {
                    if (!schedule.getDiscountTimeSlots().isEmpty()) {
                        schedule.setRegularRate(schedule.getDiscountRate());
                        schedule.setRegularTimeSlots(schedule.getDiscountTimeSlots());
                    }
                    return schedule;
                })
                .collect(Collectors.toSet());
    }

    // 根据当前用户 UID 获取商店列表，并根据当前星期几返回定价时段
    public List<StoreRes> countAvailableAndInUseByUid(String uid) {
        List<Store> stores = storeRepository.findStoresWithPoolTableCountsByUid(uid);

        return stores.stream().map(store -> {
            StoreRes storeRes = convertToAdminStoreRes(store);
            storeRes.setPricingSchedules(adjustPricingSchedulesByToday(storeRes.getPricingSchedules()));
            return storeRes;
        }).collect(Collectors.toList());
    }
}
