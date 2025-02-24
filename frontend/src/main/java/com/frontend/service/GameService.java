package com.frontend.service;


import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private GameOrderRepository gameOrderRepository;

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StorePricingScheduleRepository storePricingScheduleRepository;

    @Autowired
    private GameTransactionRecordRepository gameTransactionRecordRepository;

    public GameRecord startGame(GameReq gameReq , Long id) throws Exception {
        // 查詢用戶
        User byUid = userRepository.findById(id).get();
        PoolTable byStoreUid = poolTableRepository.findByUid(gameReq.getPoolTableUId()).get();
        Store store = storeRepository.findById(byStoreUid.getStore().getId()).get();
        var vId = store.getVendor().getId();
        Vendor vendor = vendorRepository.findById(vId).get();
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());

        // 获取当前日期对应星期几，转换为字符串
        String currentDayString = LocalDate.now().getDayOfWeek().toString().toLowerCase();  // 获取当前星期几的英文名（全小写）

// 查找当天对应的优惠时段和普通时段
        StorePricingSchedule currentSchedule = null;
        for (StorePricingSchedule schedule : pricingSchedules) {
            // 将数据库中的 "sunday", "monday" 等与当前的字符串进行比较
            if (schedule.getDayOfWeek().toLowerCase().equals(currentDayString)) {
                currentSchedule = schedule;
                break;
            }
        }

        if (currentSchedule == null) {
            throw new Exception("没有找到当天的时段信息");
        }



        // 計算價格
        int regularRateAmount = 0;
        int discountRateAmount = 0;

        discountRateAmount = currentSchedule.getDiscountRate();
        regularRateAmount = currentSchedule.getRegularRate();

        // 扣除押金
        int newAmount = byUid.getAmount() - store.getDeposit();
        if (newAmount < 0) {
            throw new Exception("儲值金不足，請儲值");
        } else {
            byUid.setAmount(newAmount);
            userRepository.save(byUid);
        }

        // 計算遊戲開始時間
        LocalDateTime startTime = LocalDateTime.now(); // 獲取當前時間戳

        // 創建遊戲紀錄並保存
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString()); // 生成UUID
        gameRecord.setStartTime(startTime);
        gameRecord.setUserUid(byUid.getUid());
        gameRecord.setPrice(store.getDeposit()); // 設置押金
        gameRecord.setStatus("STARTED"); // 設置狀態為開始
        gameRecord.setStoreId(store.getId());
        gameRecord.setStoreName(store.getName());
        gameRecord.setVendorId(vendor.getId());
        gameRecord.setVendorName(vendor.getName());
        gameRecord.setContactInfo(vendor.getContactInfo());
        gameRecord.setPoolTableId(byStoreUid.getId());
        gameRecord.setPoolTableName(byStoreUid.getTableNumber());

        // 設置一般時段的金額以及優惠時段的金額
        gameRecord.setRegularRateAmount(regularRateAmount);
        gameRecord.setDiscountRateAmount(discountRateAmount);

        gameRecordRepository.save(gameRecord);  // 儲存遊戲紀錄

        // 開啟桌台使用
        byStoreUid.setIsUse(true);
        poolTableRepository.save(byStoreUid);
        return gameRecord;
        // 返回时间戳或其他需要的資料給前端
    }

    private boolean isTimeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        // 判断当前时间是否在开始时间和结束时间之间
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }




    public GameResponse endGame(GameReq gameReq , Long id) throws Exception {
        // 取得遊戲紀錄
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());
        LocalDateTime startTime = byGameId.getStartTime();
        LocalDateTime endTime = LocalDateTime.now(); // 當前時間為結束時間
        Store store = storeRepository.findById(byGameId.getStoreId()).get();

        // 確保結束時間不早於開始時間
        if (endTime.isBefore(startTime)) {
            throw new Exception("結束時間不能早於開始時間");
        }

        // 計算遊玩時間的時間差
        Duration duration = Duration.between(startTime, endTime);  // 計算時間差
        long totalMinutes = duration.toMinutes();  // 總分鐘數

        // 計算以小時計算的總時長，向上取整
        long totalHours = (long) Math.ceil((double) totalMinutes / 60);  // 向上取整

        // 退還押金
        User byUid = userRepository.findById(id).get();
        int newAmount = byUid.getAmount() + store.getDeposit();  // 退還押金
        byUid.setAmount(newAmount);
        userRepository.save(byUid);

        // 查找 Store 的一班時段和優惠時段
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());
        String currentDayString = LocalDate.now().getDayOfWeek().toString().toLowerCase();  // 获取当前星期几的英文名（全小写）
// 查找当天对应的优惠时段和普通时段
        StorePricingSchedule currentSchedule = null;
        for (StorePricingSchedule schedule : pricingSchedules) {
            // 将数据库中的 "sunday", "monday" 等与当前的字符串进行比较
            if (schedule.getDayOfWeek().toLowerCase().equals(currentDayString)) {
                currentSchedule = schedule;
                break;
            }
        }

        if (currentSchedule == null) {
            throw new Exception("没有找到当天的时段信息");
        }

        // 计算根据时段调整的价格
        int adjustedPrice = 0;

        LocalTime currentTime = startTime.toLocalTime();
        long elapsedMinutes = totalMinutes;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        // 计算在优惠时段和普通时段内的价格
        while (elapsedMinutes > 0) {
            // 将优惠和常规时段的时间字符串转换为 LocalTime
            LocalTime discountStartTime = LocalTime.parse(currentSchedule.getDiscountStartTime(), timeFormatter);
            LocalTime discountEndTime = LocalTime.parse(currentSchedule.getDiscountEndTime(), timeFormatter);
            LocalTime regularStartTime = LocalTime.parse(currentSchedule.getRegularStartTime(), timeFormatter);
            LocalTime regularEndTime = LocalTime.parse(currentSchedule.getRegularEndTime(), timeFormatter);

            // 判断当前时间是否在优惠时段内
            if (isTimeInRange(currentTime, discountStartTime, discountEndTime)) {
                // 优惠时段内
                long discountMinutes = Math.min(elapsedMinutes, Duration.between(currentTime, discountEndTime).toMinutes());
                adjustedPrice += discountMinutes * currentSchedule.getDiscountRate() / 60.0;  // 根据分钟数计算优惠时段价格
                elapsedMinutes -= discountMinutes;
                currentTime = currentTime.plusMinutes(discountMinutes);
            }

            // 判断当前时间是否在常规时段内
            if (elapsedMinutes > 0 && isTimeInRange(currentTime, regularStartTime, regularEndTime)) {
                // 常规时段内
                long regularMinutes = Math.min(elapsedMinutes, Duration.between(currentTime, regularEndTime).toMinutes());
                adjustedPrice += regularMinutes * currentSchedule.getRegularRate() / 60.0;  // 根据分钟数计算常规时段价格
                elapsedMinutes -= regularMinutes;
                currentTime = currentTime.plusMinutes(regularMinutes);
            }

            // 如果不在时段范围内，跳到下一个时段
            if (elapsedMinutes > 0 && !isTimeInRange(currentTime, discountStartTime, discountEndTime) &&
                    !isTimeInRange(currentTime, regularStartTime, regularEndTime)) {
                // 计算完当前时段后，跳到下一个时段
                currentTime = currentTime.plusMinutes(1);  // 假设时间步长为1分钟
            }
        }
        PoolTable poolTable = poolTableRepository.findById(byGameId.getPoolTableId()).get();
        // 更新遊戲訂單紀錄
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(byUid.getUid());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setTotalPrice(adjustedPrice);  // 计算后的总价格
        gameOrder.setStartTime(startTime);
        gameOrder.setEndTime(endTime);
        gameOrder.setDuration(totalHours);
        gameOrder.setStatus("NO_PAY");
        gameOrder.setPoolTableUid(poolTable.getUid());
        gameOrderRepository.save(gameOrder);  // 儲存遊戲訂單

        // 关闭桌台使用

        poolTable.setIsUse(false);
        poolTableRepository.save(poolTable);

        byGameId.setStatus("ENDED");
        gameRecordRepository.save(byGameId);

        // 创建 GameResponse 对象返回总秒数和总金额
        GameResponse response = new GameResponse();
        response.setTotalSeconds(duration.toSeconds());
        response.setTotalPrice(adjustedPrice);

        return response;
    }

    public void checkout(CheckoutReq checkoutReq , Long id) {
        // 获取当前用户
        User user = userRepository.findById(id).get(); // 假设有一个获取当前用户的方式
        GameOrder game = gameOrderRepository.findByGameId(checkoutReq.getGameId());
        // 根据支付类型进行判断
        switch (checkoutReq.getPayType()) {
            case "1": // 儲值金支付
                if (user.getAmount() >= game.getTotalPrice()) {
                    // 扣除储值金
                    user.setAmount(user.getAmount() - game.getTotalPrice());
                    user.setTotalAmount(user.getTotalAmount() + game.getTotalPrice());
                    userRepository.save(user); // 保存更新后的用户数据
                } else {
                    throw new RuntimeException("儲值金不足");
                }
                break;

            case "2": // Apple Pay
                // 在这里处理Apple Pay支付（可以调用第三方支付接口）
                // 这里只是示意，实际支付处理需要集成相关支付SDK
                break;

            case "3": // Google Pay
                // 在这里处理Google Pay支付（可以调用第三方支付接口）
                // 这里只是示意，实际支付处理需要集成相关支付SDK
                break;

            default:
                throw new RuntimeException("无效的支付方式");
        }

        PoolTable poolTable = poolTableRepository.findByUid(game.getPoolTableUid()).get();
        Store store = storeRepository.findById(poolTable.getStore().getId()).get();
        Long id1 = store.getVendor().getId();
        Vendor vendor = vendorRepository.findById(id1).get();
        // 创建交易记录
        GameTransactionRecord transactionRecord = GameTransactionRecord.builder()
                .uid(user.getUid())
                .amount(game.getTotalPrice())
                .vendorName(vendor.getName())
                .storeName(store.getName()) // 假设有商店名
                .tableNumber(poolTable.getTableNumber()) // 假设有桌号
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .transactionType("CONSUME")
                .user(user)
                .build();

        // 保存交易记录
        gameTransactionRecordRepository.save(transactionRecord);

        game.setStatus("IS_PAY");
        gameOrderRepository.save(game);
    }
}
