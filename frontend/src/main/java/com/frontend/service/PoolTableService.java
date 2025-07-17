package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.*;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.PoolTableRepository;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.poolTable.PoolTableRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private StorePricingScheduleRepository storePricingScheduleRepository;

    // 获取今天是星期几
    private String getDayOfWeek() {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(); // 例如 "MONDAY"
    }


    public List<PoolTable> findByStoreUid(String storeUid) {
        Store store = storeRepository.findByUid(storeUid).get();
        List<PoolTable> poolTables = poolTableRepository.findByStoreId(store.getId());

        return poolTables;
    }


    public PoolTableRes getPoolTableById(String uid) throws Exception {
        PoolTable poolTable = poolTableRepository.findByUid(uid).get();
        Integer dep = 0;
        String storeName = "";
        double priceByHour = 0.0;

        if(poolTable != null) {
            Store store = storeRepository.findById(poolTable.getStore().getId()).get();
            dep = store.getDeposit();
            storeName = store.getName();

            // 計算當前時間的費率
            priceByHour = getCurrentHourlyRate(store);
        }

        if(poolTable.getIsUse() == true){
            GameRecord gameRecord = gameRecordRepository.findByPoolTableIdAndStatus(poolTable.getId() , "STARTED");
            return new PoolTableRes(dep, gameRecord.getGameId(), gameRecord.getPoolTableId(),
                    gameRecord.getPoolTableName(), gameRecord.getStoreName(), priceByHour);
        }

        return new PoolTableRes(dep, null, poolTable.getId(), poolTable.getTableNumber(),
                storeName, priceByHour);
    }


    /**
     * 獲取當前時間的小時費率
     * @param store 店舖資訊
     * @return 當前時間的小時費率
     */
    private double getCurrentHourlyRate(Store store) {
        LocalDate today = LocalDate.now();
        LocalTime currentTime = LocalTime.now();

        // 1. 首先檢查是否為特殊日期
        Optional<SpecialDate> specialDateOpt = getTodaySpecialDate(store, today);

        if (specialDateOpt.isPresent()) {
            SpecialDate specialDate = specialDateOpt.get();

            // 檢查特殊日期是否有時段設定
            if (specialDate.getTimeSlots() != null && !specialDate.getTimeSlots().isEmpty()) {
                // 尋找當前時間所在的時段
                for (SpecialTimeSlot slot : specialDate.getTimeSlots()) {
                    if (isTimeInSlot(currentTime, slot.getStartTime(), slot.getEndTime())) {
                        // 找到對應時段，返回對應費率 * 60 (轉換為小時費率)
                        if (slot.getIsDiscount()) {
                            return slot.getPrice() * 60;
                        } else {
                            return specialDate.getRegularRate() * 60;
                        }
                    }
                }
                // 如果沒有找到對應時段，使用特殊日期的一般費率
                return specialDate.getRegularRate() * 60;
            } else {
                // 特殊日期沒有具體時段，直接使用特殊日期的一般費率
                return specialDate.getRegularRate() * 60;
            }
        } else {
            // 2. 普通日期：使用正常的營業時段
            String currentDay = today.getDayOfWeek().toString();

            // 獲取當天的排程
            Optional<StorePricingSchedule> todaySchedule = store.getPricingSchedules().stream()
                    .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .findFirst();

            if (todaySchedule.isPresent()) {
                StorePricingSchedule schedule = todaySchedule.get();
                List<TimeSlot> slots = schedule.getTimeSlots();

                // 如果當天沒有時段，查找周一的時段
                if (slots == null || slots.isEmpty()) {
                    Optional<StorePricingSchedule> mondaySchedule = store.getPricingSchedules().stream()
                            .filter(s -> s.getDayOfWeek().equalsIgnoreCase("MONDAY"))
                            .findFirst();

                    if (mondaySchedule.isPresent() && !mondaySchedule.get().getTimeSlots().isEmpty()) {
                        slots = mondaySchedule.get().getTimeSlots();
                        schedule = mondaySchedule.get(); // 更新 schedule 參考
                    }
                }

                // 檢查當前時間是否在任何時段內
                if (slots != null && !slots.isEmpty()) {
                    for (TimeSlot slot : slots) {
                        if (isTimeInSlot(currentTime, slot.getStartTime(), slot.getEndTime())) {
                            // 找到對應時段，返回對應費率 * 60 (轉換為小時費率)
                            if (slot.getIsDiscount()) {
                                return schedule.getDiscountRate() * 60;
                            } else {
                                return schedule.getRegularRate() * 60;
                            }
                        }
                    }
                }

                // 如果沒有找到對應時段，使用一般費率
                return schedule.getRegularRate() * 60;
            }
        }

        // 如果都沒有找到，返回 0
        return 0.0;
    }

    /**
     * 判斷當前時間是否在指定時段內
     * @param currentTime 當前時間
     * @param startTime 開始時間
     * @param endTime 結束時間
     * @return 是否在時段內
     */
    private boolean isTimeInSlot(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        // 處理跨午夜的情況
        if (endTime.isBefore(startTime)) {
            // 跨午夜：例如 22:00 - 02:00
            return currentTime.isAfter(startTime) || currentTime.isBefore(endTime) ||
                    currentTime.equals(startTime) || currentTime.equals(endTime);
        } else {
            // 正常情況：例如 09:00 - 17:00
            return (currentTime.isAfter(startTime) || currentTime.equals(startTime)) &&
                    (currentTime.isBefore(endTime) || currentTime.equals(endTime));
        }
    }

    /**
     * 檢查特殊日期的方法
     */
    private Optional<SpecialDate> getTodaySpecialDate(Store store, LocalDate date) {
        if (store.getSpecialDates() == null) {
            return Optional.empty();
        }

        return store.getSpecialDates().stream()
                .filter(specialDate -> {
                    try {
                        // 如果日期是 LocalDate 類型
                        if (specialDate.getDate() instanceof LocalDate) {
                            return ((LocalDate) specialDate.getDate()).equals(date);
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .findFirst();
    }
}
