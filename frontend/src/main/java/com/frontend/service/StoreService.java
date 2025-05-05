package com.frontend.service;

import com.frontend.entity.store.Store;
import com.frontend.entity.store.SpecialDate;
import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.res.store.*;
import com.frontend.repo.StoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // 获取所有商店的列表
    public List<StoreRes> findAll() {
        List<Store> stores = storeRepository.findAll();
        return stores.stream()
                .map(this::convertToStoreResWithTodayInfo)
                .collect(Collectors.toList());
    }

    // 根据 ID 获取单个商店
    public StoreRes findById(Long id) {
        Store store = storeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Store not found with id: " + id));

        return convertToStoreResWithTodayInfo(store);
    }

    // 轉換 Store 實體為 StoreRes，只包含當天資訊
    private StoreRes convertToStoreResWithTodayInfo(Store store) {
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

        // 檢查今天是否為特殊日期
        Optional<SpecialDate> todaySpecialDate = getTodaySpecialDate(store);

        if (todaySpecialDate.isPresent()) {
            // 如果今天是特殊日期，使用特殊日期的資訊
            SpecialDate specialDate = todaySpecialDate.get();
            builder.openTime(specialDate.getOpenTime());
            builder.closeTime(specialDate.getCloseTime());
            builder.regularRate(specialDate.getRegularRate());

            // 設置特殊日期的時段
            List<TimeSlotRes> timeSlots = specialDate.getTimeSlots().stream()
                    .map(slot -> new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), slot.getIsDiscount()))
                    .collect(Collectors.toList());
            builder.timeSlots(timeSlots);

            // 設置特殊日期資訊
            builder.specialDateRes(convertToSpecialDateRes(specialDate));
        } else {
            // 如果不是特殊日期，使用當天的正常營業時間和價格
            String currentDay = LocalDate.now().getDayOfWeek().toString();

            Optional<StorePricingSchedule> todaySchedule = store.getPricingSchedules().stream()
                    .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .findFirst();

            if (todaySchedule.isPresent()) {
                StorePricingSchedule schedule = todaySchedule.get();
                builder.openTime(schedule.getOpenTime());
                builder.closeTime(schedule.getCloseTime());
                builder.regularRate(schedule.getRegularRate());
                builder.discountRate(schedule.getDiscountRate());

                // 只設置當天的時段
                List<TimeSlotRes> timeSlots = schedule.getTimeSlots().stream()
                        .map(slot -> new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), slot.getIsDiscount()))
                        .collect(Collectors.toList());
                builder.timeSlots(timeSlots);
            } else {
                // 如果找不到當天的排程，設置預設值
                builder.openTime(LocalTime.of(0, 0));
                builder.closeTime(LocalTime.of(23, 59));
                builder.regularRate(0);
                builder.discountRate(0);
                builder.timeSlots(List.of());
            }
        }

        return builder.build();
    }

    // 获取今天的特殊日期
    private Optional<SpecialDate> getTodaySpecialDate(Store store) {
        if (store.getSpecialDates() == null) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        return store.getSpecialDates().stream()
                .filter(specialDate -> {
                    try {
                        // 如果日期是 LocalDate 類型
                        if (specialDate.getDate() instanceof LocalDate) {
                            return ((LocalDate) specialDate.getDate()).equals(today);
                        }
                        // 如果日期是 String 類型

                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }

    // 将 SpecialDate 转换为 SpecialDateRes
    private SpecialDateRes convertToSpecialDateRes(SpecialDate specialDate) {
        List<SpecialTimeSlotRes> timeSlotResList = specialDate.getTimeSlots().stream()
                .map(slot -> new SpecialTimeSlotRes(
                        slot.getId(),
                        slot.getStartTime(),
                        slot.getEndTime(),
                        slot.getIsDiscount(),
                        slot.getPrice()
                ))
                .collect(Collectors.toList());

        String dateStr = specialDate.getDate() instanceof LocalDate
                ? ((LocalDate) specialDate.getDate()).toString()
                : specialDate.getDate().toString();

        return SpecialDateRes.builder()
                .id(specialDate.getId())
                .date(dateStr)
                .openTime(specialDate.getOpenTime())
                .closeTime(specialDate.getCloseTime())
                .regularRate(specialDate.getRegularRate())
                .timeSlots(timeSlotResList)
                .build();
    }
}