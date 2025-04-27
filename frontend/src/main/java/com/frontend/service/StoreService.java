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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;



    public List<StoreRes> findAll() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToAdminStoreRes)
                .collect(Collectors.toList());
    }

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
                    .map(pricingSchedule -> convertToStorePricingScheduleRes(pricingSchedule)) // 转换为 StorePricingScheduleRes
                    .collect(Collectors.toSet()));
        } else {
            builder.pricingSchedules(Set.of()); // 如果为 null，设为空集合
        }

        return builder.build();
    }

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


    public List<StoreRes> countAvailableAndInUseByUid(String uid) {
        // 获取当前星期几
        String currentDay = LocalDate.now().getDayOfWeek().toString();

        // 查询包含池台状态计数的商店列表
        List<Store> stores = storeRepository.findStoresWithPoolTableCountsByUid(uid);

        // 转换为StoreRes并过滤当前星期的定价时间表
        return stores.stream().map(store -> {
            // 转换为StoreRes (使用你已有的转换方法)
            StoreRes storeRes = convertToAdminStoreRes(store);

            // 过滤定价时间表，只保留当前星期的
            if (storeRes.getPricingSchedules() != null) {
                Set<StorePricingScheduleRes> filteredSchedules = storeRes.getPricingSchedules().stream()
                        .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                        .collect(Collectors.toSet());

                storeRes.setPricingSchedules(filteredSchedules);
            }

            return storeRes;
        }).collect(Collectors.toList());
    }




}

