package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.TimeSlot;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.StorePricingScheduleRes;
import com.frontend.res.store.StoreRes;
import com.frontend.res.store.TimeSlotRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.frontend.entity.store.StorePricingSchedule;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StorePricingScheduleRepository storePricingScheduleRepository;

    public Optional<List<StoreRes>> countAvailableAndInUseByUid(String uid) {
        // 获取当前星期几
        String currentDay = LocalDate.now().getDayOfWeek().toString(); // 比如 "SUNDAY"、"MONDAY" 等

        // 调用 Repository 查询方法，只获取基本信息
        Optional<List<StoreRes>> storeResList = storeRepository.countAvailableAndInUseByUid(uid, PoolTableStatus.AVAILABLE.name());

        // 手动补充 pricingSchedules 字段
        storeResList.ifPresent(storeResListResult -> {
            storeResListResult.forEach(storeRes -> {
                // 查询每个 StoreRes 的 pricingSchedules
                List<StorePricingSchedule> pricingSchedules = getPricingSchedulesForStore(storeRes.getId(), currentDay);

                Set<StorePricingScheduleRes> scheduleResList = pricingSchedules.stream()
                        .map(this::convertStorePricingScheduleToRes)
                        .collect(Collectors.toSet());

                storeRes.setPricingSchedules((List<StorePricingScheduleRes>) scheduleResList);

            });
        });

        return storeResList;
    }

    // 获取定价信息
    private List<StorePricingSchedule> getPricingSchedulesForStore(Long storeId, String currentDay) {
        // 使用更新后的查询方法
        return storePricingScheduleRepository.findByStoreIdAndDayOfWeek(storeId, currentDay);
    }

    public List<StoreRes> findAll() {
        return storeRepository.findAll().stream()
                .map(this::convert) // 使用 convert 方法转换 Store -> StoreRes
                .collect(Collectors.toList());
    }

    public StoreRes convert(Store store) {
        if (store == null) {
            return null;
        }

        // 获取池台的可用和已使用数量
        long availableCount = store.getPoolTables().stream()
                .filter(poolTable -> !poolTable.getIsUse())
                .count();

        long inUseCount = store.getPoolTables().stream()
                .filter(PoolTable::getIsUse)
                .count();

        // 获取当前日期对应星期几，转换为小写
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        String currentDayString = currentDay.toString().toLowerCase();

        // 查找当天对应的定价时段
        StorePricingSchedule currentSchedule = store.getPricingSchedules().stream()
                .filter(schedule -> schedule.getDayOfWeek().toLowerCase().equals(currentDayString))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("没有找到当天的时段信息: " + currentDayString));

        // 获取并封装所有定价时段信息
        List<StorePricingScheduleRes> pricingScheduleResList = store.getPricingSchedules().stream()
                .map(this::convertStorePricingScheduleToRes) // 使用转换方法
                .collect(Collectors.toList());

        // 返回 StoreRes 对象
        return new StoreRes(
                store.getId(),
                store.getUid(),
                store.getAddress(),
                store.getName(),
                availableCount,
                inUseCount,
                store.getLat(),
                store.getLon(),
                store.getDeposit(),
                store.getImgUrl(),
                pricingScheduleResList,
                store.getHint(),
                store.getContactPhone()
        );
    }


    // 将 StorePricingSchedule 转换为 StorePricingScheduleRes
    private StorePricingScheduleRes convertStorePricingScheduleToRes(StorePricingSchedule schedule) {
        List<TimeSlotRes> regularTimeSlotsRes = new ArrayList<>();
        List<TimeSlotRes> discountTimeSlotsRes = new ArrayList<>();

        for (TimeSlot slot : schedule.getTimeSlots()) {
            TimeSlotRes res = new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), slot.getIsDiscount());
            if (slot.getIsDiscount()) {
                discountTimeSlotsRes.add(res);
            } else {
                regularTimeSlotsRes.add(res);
            }
        }

        // rates 可以選擇用 TimeSlot 中的 rate，也可以保留 schedule 上的欄位（看你最終要不要移除那兩欄）
        return new StorePricingScheduleRes(
                schedule.getDayOfWeek(),
                regularTimeSlotsRes,
                discountTimeSlotsRes,
                schedule.getRegularRate(),   // 或者改 null
                schedule.getDiscountRate()   // 或者改 null
        );
    }

    private StorePricingScheduleRes convertToStorePricingScheduleRes(StorePricingSchedule schedule) {
        List<TimeSlotRes> timeSlotResList = schedule.getTimeSlots().stream()
                .map(slot -> new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), slot.getIsDiscount()))
                .collect(Collectors.toList());

        return StorePricingScheduleRes.builder()
                .dayOfWeek(schedule.getDayOfWeek())
                .regularRate(schedule.getRegularRate())
                .discountRate(schedule.getDiscountRate())
                .regularTimeSlots(schedule.getTimeSlots().stream()
                        .filter(slot -> !slot.getIsDiscount())
                        .map(slot -> new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), false))
                        .collect(Collectors.toList()))
                .discountTimeSlots(schedule.getTimeSlots().stream()
                        .filter(TimeSlot::getIsDiscount)
                        .map(slot -> new TimeSlotRes(slot.getStartTime(), slot.getEndTime(), true))
                        .collect(Collectors.toList()))
                .build();

    }


}

