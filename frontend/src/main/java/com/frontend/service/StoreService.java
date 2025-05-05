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
import java.util.Set;
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
                .cancelBookTime(store.getCancelBookTime())
                .user(store.getUser());

        // 檢查今天是否為特殊日期
        Optional<SpecialDate> todaySpecialDate = getTodaySpecialDate(store);

        if (todaySpecialDate.isPresent()) {
            // 如果今天是特殊日期，使用特殊日期的資訊
            SpecialDate specialDate = todaySpecialDate.get();

            // 只獲取 isDiscount 為 true 的時段
            List<TimeSlotRes> discountTimeSlots = specialDate.getTimeSlots().stream()
                    .filter(slot -> slot.getIsDiscount()) // 只過濾優惠時段
                    .map(slot -> {
                        TimeSlotRes timeSlotRes = new TimeSlotRes();
                        timeSlotRes.setStartTime(slot.getStartTime());
                        timeSlotRes.setEndTime(slot.getEndTime());
                        timeSlotRes.setIsDiscount(true);
                        timeSlotRes.setRegularRate(slot.getPrice()); // 設置特殊價格
                        return timeSlotRes;
                    })
                    .collect(Collectors.toList());

            // 設置 todayRes
            TodayRes todayRes = TodayRes.builder()
                    .isSpecialDate(true)
                    .date(specialDate.getDate() instanceof LocalDate
                            ? ((LocalDate) specialDate.getDate()).toString()
                            : specialDate.getDate().toString())
                    .openTime(specialDate.getOpenTime())
                    .closeTime(specialDate.getCloseTime())
                    .regularRate(specialDate.getRegularRate())
                    .timeSlots(discountTimeSlots) // 只包含優惠時段
                    .build();

            builder.todayRes(todayRes);

            // 設置 pricingSchedules
            StorePricingScheduleRes specialSchedule = StorePricingScheduleRes.builder()
                    .dayOfWeek(LocalDate.now().getDayOfWeek().toString())
                    .openTime(specialDate.getOpenTime())
                    .closeTime(specialDate.getCloseTime())
                    .regularRate(specialDate.getRegularRate())
                    .regularTimeSlots(List.of())
                    .discountTimeSlots(discountTimeSlots)
                    .build();

            builder.pricingSchedules(Set.of(specialSchedule));
        } else {
            // 如果不是特殊日期，使用當天的正常營業時間和價格
            String currentDay = LocalDate.now().getDayOfWeek().toString();

            // 先嘗試獲取當天的排程
            Optional<StorePricingSchedule> todaySchedule = store.getPricingSchedules().stream()
                    .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .findFirst();

            if (todaySchedule.isPresent()) {
                StorePricingSchedule schedule = todaySchedule.get();

                // 獲取時段 - 先看當天的時段，如果為空則查找週一的時段
                List<TimeSlot> slots = schedule.getTimeSlots();
                if (slots == null || slots.isEmpty()) {
                    // 如果當天沒有時段，查找週一的時段
                    Optional<StorePricingSchedule> mondaySchedule = store.getPricingSchedules().stream()
                            .filter(s -> s.getDayOfWeek().equalsIgnoreCase("MONDAY"))
                            .findFirst();

                    if (mondaySchedule.isPresent() && !mondaySchedule.get().getTimeSlots().isEmpty()) {
                        slots = mondaySchedule.get().getTimeSlots();
                    }
                }

                // 只獲取 isDiscount 為 true 的時段
                List<TimeSlotRes> discountTimeSlots = slots.stream()
                        .filter(slot -> slot.getIsDiscount()) // 只過濾優惠時段
                        .map(slot -> {
                            TimeSlotRes timeSlotRes = new TimeSlotRes();
                            timeSlotRes.setStartTime(slot.getStartTime());
                            timeSlotRes.setEndTime(slot.getEndTime());
                            timeSlotRes.setIsDiscount(true);
                            timeSlotRes.setRegularRate(schedule.getDiscountRate()); // 設置優惠價格
                            return timeSlotRes;
                        })
                        .collect(Collectors.toList());

                // 設置 todayRes
                TodayRes todayRes = TodayRes.builder()
                        .isSpecialDate(false)
                        .date(LocalDate.now().toString())
                        .openTime(schedule.getOpenTime())
                        .closeTime(schedule.getCloseTime())
                        .regularRate(schedule.getRegularRate())
                        .discountRate(schedule.getDiscountRate())
                        .timeSlots(discountTimeSlots) // 只包含優惠時段
                        .build();

                builder.todayRes(todayRes);

                // 設置 pricingSchedules
                if (store.getPricingSchedules() != null) {
                    Set<StorePricingScheduleRes> pricingSchedules = store.getPricingSchedules().stream()
                            .map(this::convertToStorePricingScheduleRes)
                            .collect(Collectors.toSet());
                    builder.pricingSchedules(pricingSchedules);
                } else {
                    builder.pricingSchedules(Set.of());
                }
            } else {
                // 如果找不到當天的排程，設置預設值
                TodayRes todayRes = TodayRes.builder()
                        .isSpecialDate(false)
                        .date(LocalDate.now().toString())
                        .openTime(LocalTime.of(0, 0))
                        .closeTime(LocalTime.of(23, 59))
                        .regularRate(0)
                        .discountRate(0)
                        .timeSlots(List.of()) // 空列表，因為沒有時段
                        .build();

                builder.todayRes(todayRes);
                builder.pricingSchedules(Set.of());
            }
        }

        return builder.build();
    }

    private StorePricingScheduleRes convertToStorePricingScheduleRes(StorePricingSchedule schedule) {
        List<TimeSlot> slots = schedule.getTimeSlots();
        if (slots == null || slots.isEmpty()) {
            // 如果當天沒有時段，查找週一的時段
            if (!schedule.getDayOfWeek().equalsIgnoreCase("MONDAY")) {
                Optional<StorePricingSchedule> mondaySchedule = schedule.getStore().getPricingSchedules().stream()
                        .filter(s -> s.getDayOfWeek().equalsIgnoreCase("MONDAY"))
                        .findFirst();

                if (mondaySchedule.isPresent() && !mondaySchedule.get().getTimeSlots().isEmpty()) {
                    slots = mondaySchedule.get().getTimeSlots();
                }
            }
        }

        // 轉換所有時段（包括一般和優惠時段）
        List<TimeSlotRes> regularSlots = slots.stream()
                .filter(slot -> !slot.getIsDiscount())
                .map(slot -> {
                    TimeSlotRes timeSlotRes = new TimeSlotRes();
                    timeSlotRes.setStartTime(slot.getStartTime());
                    timeSlotRes.setEndTime(slot.getEndTime());
                    timeSlotRes.setIsDiscount(false);
                    timeSlotRes.setRegularRate(schedule.getRegularRate());
                    return timeSlotRes;
                })
                .collect(Collectors.toList());

        List<TimeSlotRes> discountSlots = slots.stream()
                .filter(TimeSlot::getIsDiscount)
                .map(slot -> {
                    TimeSlotRes timeSlotRes = new TimeSlotRes();
                    timeSlotRes.setStartTime(slot.getStartTime());
                    timeSlotRes.setEndTime(slot.getEndTime());
                    timeSlotRes.setIsDiscount(true);
                    timeSlotRes.setRegularRate(schedule.getDiscountRate());
                    return timeSlotRes;
                })
                .collect(Collectors.toList());

        return StorePricingScheduleRes.builder()
                .dayOfWeek(schedule.getDayOfWeek())
                .openTime(schedule.getOpenTime())
                .closeTime(schedule.getCloseTime())
                .regularRate(schedule.getRegularRate())
                .discountRate(schedule.getDiscountRate())
                .regularTimeSlots(regularSlots)
                .discountTimeSlots(discountSlots)
                .build();
    }

    // 获取今天的特殊日期
    private Optional<SpecialDate> getTodaySpecialDate(Store store) {
        if (store.getSpecialDates() == null) {
            return Optional.empty();
        }

        LocalDate today = LocalDate.now();
        String todayStr = today.toString(); // 形如 "2025-05-06"

        return store.getSpecialDates().stream()
                .filter(specialDate -> {
                    try {
                        // 如果日期是 LocalDate 類型
                        if (specialDate.getDate() instanceof LocalDate) {
                            return ((LocalDate) specialDate.getDate()).equals(today);
                        }
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