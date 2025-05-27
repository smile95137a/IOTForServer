package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.store.*;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.UserRepository;
import com.frontend.req.store.TimeSlotInfo;
import com.frontend.res.game.GameRecordRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<GameRecordRes> getGameRecordsByUserUidAndStatus(Long id) {
        // 安全地获取 User 对象
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 获取状态为 "STARTED" 的游戏记录
        List<GameRecord> started = gameRecordRepository.findByUserUidAndStatus(user.getUid(), "STARTED");

        // 如果没有找到任何游戏记录，返回空列表
        if (started.isEmpty()) {
            return Collections.emptyList();
        }

        List<GameRecordRes> result = new ArrayList<>();

        for (GameRecord game : started) {
            // 安全地获取 Store 对象
            Long storeId = game.getStoreId();
            Store store = storeRepository.findById(storeId)
                    .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

            // 设置游戏记录的提示信息
            game.setHint(store.getHint());

            // 获取厂商信息
            Vendor vendor = store.getVendor();

            // 检查游戏开始日期是否为特殊日期
            LocalDate gameDate = game.getStartTime().toLocalDate();
            Optional<SpecialDate> specialDateOpt = getTodaySpecialDate(store, gameDate);

            // 如果是特殊日期，更新 GameRecord 的价格信息
            if (specialDateOpt.isPresent()) {
                SpecialDate specialDate = specialDateOpt.get();
                // 临时更新价格信息，但不保存到数据库
                game.setRegularRateAmount(specialDate.getRegularRate());
            }

            // 转换为 GameRecordRes 对象
            GameRecordRes gameRecordRes = convertToGameRecordRes(game, vendor, store);

            // **新增：获取当天所有时段信息**
            List<TimeSlotInfo> allTimeSlots = getAllTimeSlotsForDate(store, gameDate);
            gameRecordRes.setTimeSlots(allTimeSlots);

            result.add(gameRecordRes);
        }

        return result;
    }

    /**
     * 获取指定日期的所有时段信息
     * @param store 店铺信息
     * @param targetDate 目标日期
     * @return 当天所有时段信息列表
     */
    private List<TimeSlotInfo> getAllTimeSlotsForDate(Store store, LocalDate targetDate) {
        List<TimeSlotInfo> timeSlots = new ArrayList<>();

        // 1. 首先检查是否为特殊日期
        Optional<SpecialDate> specialDateOpt = getTodaySpecialDate(store, targetDate);

        if (specialDateOpt.isPresent()) {
            // 特殊日期：使用特殊日期的时段
            SpecialDate specialDate = specialDateOpt.get();

            if (specialDate.getTimeSlots() != null && !specialDate.getTimeSlots().isEmpty()) {
                // 转换特殊日期的时段
                for (SpecialTimeSlot slot : specialDate.getTimeSlots()) {
                    TimeSlotInfo timeSlotInfo = TimeSlotInfo.builder()
                            .isDiscount(slot.getIsDiscount())
                            .startTime(slot.getStartTime())
                            .endTime(slot.getEndTime())
                            .rate(slot.getIsDiscount() ? slot.getPrice() : specialDate.getRegularRate())
                            .isSpecialDate(true)
                            .timeSlotType(slot.getIsDiscount() ? "DISCOUNT" : "REGULAR")
                            .build();
                    timeSlots.add(timeSlotInfo);
                }
            } else {
                // 特殊日期没有具体时段，返回整天作为一个时段
                TimeSlotInfo timeSlotInfo = TimeSlotInfo.builder()
                        .isDiscount(false)
                        .startTime(specialDate.getOpenTime())
                        .endTime(specialDate.getCloseTime())
                        .rate(specialDate.getRegularRate())
                        .isSpecialDate(true)
                        .timeSlotType("REGULAR")
                        .build();
                timeSlots.add(timeSlotInfo);
            }
        } else {
            // 2. 普通日期：使用正常的营业时段
            String currentDay = targetDate.getDayOfWeek().toString();

            // 获取当天的排程
            Optional<StorePricingSchedule> todaySchedule = store.getPricingSchedules().stream()
                    .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(currentDay))
                    .findFirst();

            if (todaySchedule.isPresent()) {
                StorePricingSchedule schedule = todaySchedule.get();
                List<TimeSlot> slots = schedule.getTimeSlots();

                // 如果当天没有时段，查找周一的时段
                if (slots == null || slots.isEmpty()) {
                    Optional<StorePricingSchedule> mondaySchedule = store.getPricingSchedules().stream()
                            .filter(s -> s.getDayOfWeek().equalsIgnoreCase("MONDAY"))
                            .findFirst();

                    if (mondaySchedule.isPresent() && !mondaySchedule.get().getTimeSlots().isEmpty()) {
                        slots = mondaySchedule.get().getTimeSlots();
                    }
                }

                // 转换所有时段
                if (slots != null && !slots.isEmpty()) {
                    for (TimeSlot slot : slots) {
                        TimeSlotInfo timeSlotInfo = TimeSlotInfo.builder()
                                .isDiscount(slot.getIsDiscount())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .rate(slot.getIsDiscount() ? schedule.getDiscountRate() : schedule.getRegularRate())
                                .isSpecialDate(false)
                                .timeSlotType(slot.getIsDiscount() ? "DISCOUNT" : "REGULAR")
                                .build();
                        timeSlots.add(timeSlotInfo);
                    }
                } else {
                    // 没有具体时段，返回整个营业时间作为一个时段
                    TimeSlotInfo timeSlotInfo = TimeSlotInfo.builder()
                            .isDiscount(false)
                            .startTime(schedule.getOpenTime())
                            .endTime(schedule.getCloseTime())
                            .rate(schedule.getRegularRate())
                            .isSpecialDate(false)
                            .timeSlotType("REGULAR")
                            .build();
                    timeSlots.add(timeSlotInfo);
                }
            }
        }

        // 按开始时间排序
        timeSlots.sort((a, b) -> a.getStartTime().compareTo(b.getStartTime()));

        return timeSlots;
    }

    /**
     * 判断当前时间是否在指定时段内
     * @param currentTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 是否在时段内
     */
    private boolean isTimeInSlot(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        // 处理跨午夜的情况
        if (endTime.isBefore(startTime)) {
            // 跨午夜：例如 22:00 - 02:00
            return currentTime.isAfter(startTime) || currentTime.isBefore(endTime) ||
                    currentTime.equals(startTime) || currentTime.equals(endTime);
        } else {
            // 正常情况：例如 09:00 - 17:00
            return (currentTime.isAfter(startTime) || currentTime.equals(startTime)) &&
                    (currentTime.isBefore(endTime) || currentTime.equals(endTime));
        }
    }
    // 根据 ID 获取特殊日期的方法（需要实现）
    private Optional<SpecialDate> getSpecialDateById(Long specialDateId) {
        // 这里需要实现通过 ID 获取特殊日期的逻辑
        // 可能需要添加一个 SpecialDateRepository
        // 暂时返回空
        return Optional.empty();
    }

    // 检查特殊日期的方法（与其他方法相同）
    private Optional<SpecialDate> getTodaySpecialDate(Store store, LocalDate date) {
        if (store.getSpecialDates() == null) {
            return Optional.empty();
        }

        String dateStr = date.toString(); // 形如 "2025-05-06"

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

    public List<GameRecordRes> getGameRecordsByUserUid(Long id) {
        // 安全地获取 User 对象
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 获取该用户的所有 GameRecord
        List<GameRecord> allGameRecords = gameRecordRepository.findByUserUid(user.getUid());

        // 如果没有找到任何记录，返回空列表
        if (allGameRecords.isEmpty()) {
            return Collections.emptyList(); // 或者抛出异常，依据需求决定
        }

        // 假设所有记录的 storeId 相同，获取第一个记录的 storeId
        Long storeId = allGameRecords.get(0).getStoreId();

        // 安全地获取 Store 对象
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        // 设置所有 GameRecord 的提示信息
        allGameRecords.forEach(x -> x.setHint(store.getHint()));


        Vendor vendor = store.getVendor();
        List<GameRecordRes> result = new ArrayList<>();
        allGameRecords.forEach(game -> {
            GameRecordRes gameRecordRes = convertToGameRecordRes(game, vendor , store);
            result.add(gameRecordRes);
        });
        return result;
    }


    public GameRecordRes convertToGameRecordRes(GameRecord gameRecord, Vendor vendor , Store store) {
        GameRecordRes res = new GameRecordRes();
        res.setId(gameRecord.getId());
        res.setGameId(gameRecord.getGameId());
        res.setStartTime(gameRecord.getStartTime());
        res.setUserUid(gameRecord.getUserUid());
        res.setPrice(gameRecord.getPrice());
        res.setStatus(gameRecord.getStatus());
        res.setStoreId(gameRecord.getStoreId());
        res.setStoreName(gameRecord.getStoreName());
        res.setVendorId(gameRecord.getVendorId());
        res.setVendorName(gameRecord.getVendorName());
        res.setContactInfo(gameRecord.getContactInfo());
        res.setPoolTableId(gameRecord.getPoolTableId());
        res.setPoolTableName(gameRecord.getPoolTableName());
        res.setStorePhone(store.getContactPhone());
        res.setRegularRateAmount(gameRecord.getRegularRateAmount());
        res.setDiscountRateAmount(gameRecord.getDiscountRateAmount());
        res.setHint(gameRecord.getHint());
        res.setVendor(vendor);
        return res;
    }

}
