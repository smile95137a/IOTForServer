package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
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
import java.util.List;
import java.util.Optional;
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
                List<StorePricingScheduleRes> pricingSchedules = getPricingSchedulesForStore(storeRes.getId(), currentDay);
                storeRes.setPricingSchedules(pricingSchedules);
            });
        });

        return storeResList;
    }

    // 获取定价信息
    private List<StorePricingScheduleRes> getPricingSchedulesForStore(Long storeId, String currentDay) {
        // 使用更新后的查询方法
//        return storePricingScheduleRepository.findByStoreIdAndDayOfWeek(storeId, currentDay);
        return null;
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
        // 将普通时段和优惠时段分别转换为 TimeSlotRes
        List<TimeSlotRes> regularTimeSlotsRes = schedule.getTimeSlots().stream()
                .filter(timeSlot -> !timeSlot.getIsDiscount())  // 筛选出普通时段
                .map(timeSlot -> new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), false)) // 普通时段
                .collect(Collectors.toList());

        List<TimeSlotRes> discountTimeSlotsRes = schedule.getTimeSlots().stream()
                .filter(timeSlot -> timeSlot.getIsDiscount())  // 筛选出优惠时段
                .map(timeSlot -> new TimeSlotRes(timeSlot.getStartTime(), timeSlot.getEndTime(), true)) // 优惠时段
                .collect(Collectors.toList());

        // 返回转换后的 StorePricingScheduleRes，包含分开的普通时段和优惠时段
        return new StorePricingScheduleRes(
                schedule.getDayOfWeek(),
                regularTimeSlotsRes,     // 普通时段
                discountTimeSlotsRes,    // 优惠时段
                schedule.getRegularRate(),
                schedule.getDiscountRate()
        );
    }


}

