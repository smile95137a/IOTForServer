package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.StorePricingScheduleRes;
import com.frontend.res.store.StoreRes;
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
                List<StorePricingScheduleRes> pricingSchedules = getPricingSchedulesForStore(storeRes.getStoreId(), currentDay);
                storeRes.setPricingSchedules(pricingSchedules);
            });
        });

        return storeResList;
    }

    // 获取定价信息
    private List<StorePricingScheduleRes> getPricingSchedulesForStore(Long storeId, String currentDay) {
        // 使用更新后的查询方法
        return storePricingScheduleRepository.findByStoreIdAndDayOfWeek(storeId, currentDay);
    }





    public List<StoreRes> findAll() {
        return storeRepository.findAll().stream()
                .map(StoreService::convert) // 使用 convert 方法转换 Store -> StoreRes
                .toList();
    }


    public static StoreRes convert(Store store) {
        if (store == null) {
            return null;
        }

        long availableCount = store.getPoolTables().stream()
                .filter(poolTable -> !poolTable.getIsUse())
                .count();

        long inUseCount = store.getPoolTables().stream()
                .filter(PoolTable::getIsUse)
                .count();

        // 获取当前日期对应星期几，转换为小写
        DayOfWeek currentDay = LocalDate.now().getDayOfWeek();
        String currentDayString = currentDay.toString().toLowerCase();

        System.out.println("Current day: [" + currentDayString + "]");  // 打印当前星期几
        System.out.println("Current day length: " + currentDayString.length());

        // 查找当天对应的定价时段
        StorePricingSchedule currentSchedule = store.getPricingSchedules().stream()
                .peek(schedule -> {
                    String dayOfWeek = schedule.getDayOfWeek().trim().toLowerCase();
                    System.out.println("Schedule day: [" + dayOfWeek + "]");
                    System.out.println("Schedule day length: " + dayOfWeek.length());
                    System.out.println("Comparison result: " + dayOfWeek.equals(currentDayString));

                    // 打印字符的ASCII码，查看是否有隐藏字符
                    System.out.println("Schedule day ASCII: " + dayOfWeek.chars()
                            .mapToObj(ch -> String.format("%d ", ch))
                            .collect(Collectors.joining()));
                    System.out.println("Current day ASCII: " + currentDayString.chars()
                            .mapToObj(ch -> String.format("%d ", ch))
                            .collect(Collectors.joining()));
                })
                .filter(schedule -> {
                    String dayOfWeek = schedule.getDayOfWeek().trim().toLowerCase();
                    return dayOfWeek.equals(currentDayString);  // 确保比较时去空格并统一为小写
                })
                .findFirst()
                .orElseThrow(() -> new RuntimeException("没有找到当天的时段信息: " + currentDayString));

        // 获取并封装所有定价时段信息
        List<StorePricingScheduleRes> pricingScheduleResList = store.getPricingSchedules().stream()
                .map(schedule -> new StorePricingScheduleRes(
                        schedule.getDayOfWeek(),
                        schedule.getRegularStartTime(),
                        schedule.getRegularEndTime(),
                        schedule.getRegularRate(),
                        schedule.getDiscountStartTime(),
                        schedule.getDiscountEndTime(),
                        schedule.getDiscountRate()
                ))
                .collect(Collectors.toList());

        // 返回StoreRes对象
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
                pricingScheduleResList

        );
    }






}
