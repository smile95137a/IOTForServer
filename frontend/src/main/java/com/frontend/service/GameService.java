package com.frontend.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.game.BookGameReq;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameRes;
import com.frontend.res.game.GameResponse;
import com.frontend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired
    private TableEquipmentRepository tableEquipmentRepository;

    public GameRecord bookStartGame(GameReq gameReq) throws Exception {
        // 查詢用戶
        User byUid = userRepository.findById(SecurityUtils.getSecurityUser().getId()).get();
        PoolTable byStoreUid = poolTableRepository.findByUid(gameReq.getPoolTableUId()).get();
        Store store = storeRepository.findById(byStoreUid.getStore().getId()).get();
        var vId = store.getVendor().getId();

        // 查找遊戲紀錄
        GameRecord gameRecord = gameRecordRepository.findByGameId(gameReq.getGameId());
        if (gameRecord == null) {
            throw new Exception("無預定球局");
        }

        if (!"BOOK".equals(gameRecord.getStatus())) {
            throw new Exception("無預定球局");
        }
        GameOrder byGameId = gameOrderRepository.findByGameId(gameReq.getGameId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = byGameId.getStartTime();

        LocalDateTime endTime = byGameId.getEndTime();

        if (now.isBefore(startTime)) {
            throw new Exception("未達預約時間");
        }
        else if (now.isAfter(endTime)) {
            System.out.println("已超過預約時間");
        }

        // 更新遊戲紀錄狀態並儲存
        gameRecord.setStatus("STARTED");
        gameRecord.setStartTime(startTime);
        gameRecordRepository.save(gameRecord);

        // 開啟桌台使用
        byStoreUid.setIsUse(true);
        poolTableRepository.save(byStoreUid);

        return gameRecord;
    }

    public GameRes startGame(GameReq gameReq , Long id) throws Exception {
        // 查詢用戶
        User byUid = userRepository.findById(id).get();
        PoolTable byStoreUid = poolTableRepository.findByUid(gameReq.getPoolTableUId()).get();
        Store store = storeRepository.findById(byStoreUid.getStore().getId()).get();
        Long vId = store.getVendor().getId();
        Vendor vendor = vendorRepository.findById(vId).get();
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());

        // 检查是否已经有正在进行的游戏
        boolean isUse = gameIsUse(byUid.getUid());
        if (isUse) {
            throw new Exception("已經有開放中的球局");
        }

        // 获取当前日期对应星期几，转换为字符串
        String currentDayString = LocalDate.now().getDayOfWeek().toString().toLowerCase();  // 获取当前星期几的英文名（全小写）

        // 查找当天对应的优惠时段和普通时段
        StorePricingSchedule currentSchedule = null;
        for (StorePricingSchedule schedule : pricingSchedules) {
            if (schedule.getDayOfWeek().toLowerCase().equals(currentDayString)) {
                currentSchedule = schedule;
                break;
            }
        }

        if (currentSchedule == null) {
            throw new Exception("沒有找到當天的訊息");
        }

        // 计算价格
        int regularRateAmount = currentSchedule.getRegularRate();
        int discountRateAmount = currentSchedule.getDiscountRate();

        // 扣除押金
        int newAmount = byUid.getAmount() - store.getDeposit();
        if (newAmount < 0) {
            throw new Exception("儲值金不足，請儲值");
        } else {
            byUid.setAmount(newAmount);
            userRepository.save(byUid);
        }

        // 获取当前时间并检查是否有预定的游戏时间
        LocalDateTime startTime = LocalDateTime.now();

        // 查找当天是否有预定的游戏记录（状态为 BOOKED）
        List<String> bookedGames = gameRecordRepository.findGameIdByStoreIdAndStatus(
                store.getId(),
                "BOOKED"
        );
        String message = "";
        long endTimeMinutes = 0;
        // 检查是否有冲突的预定
        for (String bookedGame : bookedGames) {
            // 查找预定的订单，并获取该订单的开始时间
            GameOrder order = gameOrderRepository.findByGameId(bookedGame);
            if (order == null) {
                continue; // 如果找不到对应的订单，则跳过
            }

            LocalDateTime bookedStartTime = order.getStartTime(); // 获取预定的开始时间
            LocalDateTime bookedEndTime = order.getEndTime(); // 获取预定的结束时间（从订单中获取）

            if (startTime.isBefore(bookedEndTime) && startTime.plusHours(1).isAfter(bookedStartTime)) {
                // 当前时间与预定时间冲突，通知用户
                long availableTimeMinutes = Duration.between(startTime, bookedStartTime).toMinutes();
                endTimeMinutes += availableTimeMinutes + 5; // 用户只能玩到预定结束前5分钟

                // 创建通知信息
                message = "您的遊戲時間 " + bookedEndTime.minusMinutes(5).toLocalTime() + "，之後將會結束並計算費用。";

                // 如果是立即开台，计算可用时间
                startTime = bookedEndTime.minusMinutes(5); // 设置游戏实际开始时间
                break; // 退出循环，使用更新后的开始时间
            }
        }

        // 创建游戏记录并保存
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString()); // 生成UUID
        gameRecord.setStartTime(startTime);
        gameRecord.setUserUid(byUid.getUid());
        gameRecord.setPrice(store.getDeposit()); // 设置押金
        gameRecord.setStatus("STARTED"); // 设置状态为开始
        gameRecord.setStoreId(store.getId());
        gameRecord.setStoreName(store.getName());
        gameRecord.setVendorId(vendor.getId());
        gameRecord.setVendorName(vendor.getName());
        gameRecord.setContactInfo(vendor.getContactInfo());
        gameRecord.setPoolTableId(byStoreUid.getId());
        gameRecord.setPoolTableName(byStoreUid.getTableNumber());

        // 设置普通时段金额和优惠时段金额
        gameRecord.setRegularRateAmount(regularRateAmount);
        gameRecord.setDiscountRateAmount(discountRateAmount);

        gameRecordRepository.save(gameRecord);  // 保存游戏记录

        // 开启桌台使用
        byStoreUid.setIsUse(true);
        poolTableRepository.save(byStoreUid);

        //開啟該桌台的所有設備
        List<TableEquipment> byPoolTableId = tableEquipmentRepository.findByPoolTableId(byStoreUid.getId());
        for(TableEquipment table : byPoolTableId){
            table.setStatus(true);
            tableEquipmentRepository.save(table);
        }


        GameRes gameRes = new GameRes(gameRecord , message , endTimeMinutes);


        return gameRes;
    }


    private boolean isTimeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        // 判断当前时间是否在开始时间和结束时间之间
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    private boolean gameIsUse(String uid){
        boolean isUse = false;
        List<GameRecord> started = gameRecordRepository.findByUserUidAndStatus(uid, "STARTED");
        if(!started.isEmpty()){
            isUse = true;
           
        }

        return isUse;
    }


    public GameResponse endGame(GameReq gameReq, Long id) throws Exception {
        // 取得遊戲紀錄
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());

        // 当前时间（结束时间）
        LocalDateTime endDateTime = LocalDateTime.now();
        LocalDateTime startTimeInTaipei = byGameId.getStartTime();

        if (endDateTime.isBefore(startTimeInTaipei)) {
            throw new Exception("结束时间不能早于开始时间");
        }

        Store store = storeRepository.findById(byGameId.getStoreId()).orElseThrow(() -> new Exception("店家信息未找到"));

        // 計算遊玩時間的時間差
        Duration duration = Duration.between(startTimeInTaipei, endDateTime);  // 計算時間差
        long totalMinutes = duration.toMinutes();  // 總分鐘數

        // 計算以小時計算的總時長，向上取整
        long totalHours = (long) Math.ceil((double) totalMinutes / 60);  // 向上取整

        // 退還押金
        User byUid = userRepository.findById(id).orElseThrow(() -> new Exception("用户信息未找到"));
        int newAmount = byUid.getAmount() + store.getDeposit();  // 退還押金
        byUid.setAmount(newAmount);
        userRepository.save(byUid);

        // 查找 Store 的一班時段和優惠時段
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());
        String currentDayString = LocalDate.now().getDayOfWeek().toString().toLowerCase();  // 获取当前星期几的英文名（全小写）

        // 查找当天对应的优惠时段和普通时段
        StorePricingSchedule currentSchedule = null;
        for (StorePricingSchedule schedule : pricingSchedules) {
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

        LocalTime currentTime = startTimeInTaipei.toLocalTime();
        long elapsedMinutes = totalMinutes;
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        Set<TimeSlot> timeSlots = currentSchedule.getTimeSlots(); // ← 要補上這行

        while (elapsedMinutes > 0) {
            // 找出目前時間落在哪個 slot
            LocalTime finalCurrentTime = currentTime;
            Optional<TimeSlot> matchedSlotOpt = timeSlots.stream()
                    .filter(slot -> isTimeInRange(finalCurrentTime, slot.getStartTime(), slot.getEndTime()))
                    .findFirst();

            if (matchedSlotOpt.isPresent()) {
                TimeSlot slot = matchedSlotOpt.get();
                LocalTime slotEnd = slot.getEndTime();
                long minutesInSlot = Math.min(elapsedMinutes, Duration.between(currentTime, slotEnd).toMinutes());

                int rate = slot.getIsDiscount() ? currentSchedule.getDiscountRate() : currentSchedule.getRegularRate();
                adjustedPrice += minutesInSlot * rate / 60.0;

                currentTime = currentTime.plusMinutes(minutesInSlot);
                elapsedMinutes -= minutesInSlot;
            } else {
                // 若不落在任何 slot，就跳過該分鐘
                currentTime = currentTime.plusMinutes(1);
                elapsedMinutes--;
            }
        }



        PoolTable poolTable = poolTableRepository.findById(byGameId.getPoolTableId()).orElseThrow(() -> new Exception("桌台信息未找到"));

        // 更新遊戲訂單紀錄
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(byUid.getUid());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setTotalPrice(adjustedPrice);
        gameOrder.setStartTime(startTimeInTaipei);
        gameOrder.setEndTime(endDateTime);
        gameOrder.setDuration(totalHours);
        gameOrder.setStatus("NO_PAY");
        gameOrder.setPoolTableUid(poolTable.getUid());
        gameOrderRepository.save(gameOrder);

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

    public GameRecord bookGame(BookGameReq gameReq) throws Exception {
        User byUid = userRepository.findById(SecurityUtils.getSecurityUser().getId()).orElseThrow(
                () -> new Exception("使用者不存在"));

        boolean isUse = gameIsUse(byUid.getUid());
        if (isUse) {
            throw new Exception("已經有開放中的球局");
        }

        PoolTable byStoreUid = poolTableRepository.findByUid(gameReq.getPoolTableUId()).orElseThrow(
                () -> new Exception("桌球桌不存在"));

        Store store = storeRepository.findById(byStoreUid.getStore().getId()).orElseThrow(
                () -> new Exception("店家不存在"));

        Vendor vendor = vendorRepository.findById(store.getVendor().getId()).orElseThrow(
                () -> new Exception("業主不存在"));

        // ➡️ 使用選擇的日期來判斷
        String bookingDayString = gameReq.getBookDate().getDayOfWeek().toString().toLowerCase();
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());

        StorePricingSchedule currentSchedule = pricingSchedules.stream()
                .filter(schedule -> schedule.getDayOfWeek().toLowerCase().equals(bookingDayString))
                .findFirst()
                .orElseThrow(() -> new Exception("沒有找到當天的優惠或定價訊息"));

        // ➡️ 計算費率
        int discountRateAmount = currentSchedule.getDiscountRate();
        int regularRateAmount = currentSchedule.getRegularRate();
        int bookDeposit = store.getDeposit() * (store.getBookTime() == 0 ? 1 : store.getBookTime());

        // ➡️ 查詢是否有該遊戲已被預約
        List<String> gameIds = gameRecordRepository.findGameIdByPoolTableIdAndStatus(
                byStoreUid.getId(), "BOOK"
        );

// ➡️ 根據找到的 gameIds，檢查是否有對應的 GameOrder 預約紀錄
        // 假設您的時區是 UTC+8
        ZoneId zoneId = ZoneId.of("Asia/Taipei"); // 設定您所需的時區

// 將 LocalDateTime 轉換為 ZonedDateTime
        LocalDateTime startOfDay = gameReq.getStartTime();

        LocalDateTime endOfDay = startOfDay.plusDays(1).minusSeconds(1); // 當天的範圍 00:00 - 23:59:59

// 查詢資料
        List<GameOrder> existingBookings = gameOrderRepository.findByGameIdInAndStartTimeBetween(
                gameIds,
                startOfDay,
                endOfDay
        );

// 檢查是否有預約
        if (!existingBookings.isEmpty()) {
            throw new Exception("該時間段已被預約");
        }

        // ➡️ 扣款
        int newAmount = byUid.getAmount() - bookDeposit;
        if (newAmount < 0) {
            throw new Exception("儲值金不足，請儲值");
        } else {
            byUid.setAmount(newAmount);
            userRepository.save(byUid);
        }
        LocalDateTime endTime = gameReq.getEndTime();
        // ➡️ 建立 GameRecord
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString());
        gameRecord.setUserUid(byUid.getUid());
        gameRecord.setStartTime(startOfDay);
        gameRecord.setPrice(bookDeposit);
        gameRecord.setRegularRateAmount(regularRateAmount);
        gameRecord.setDiscountRateAmount(discountRateAmount);
        gameRecord.setStatus("BOOK");
        gameRecord.setStoreId(store.getId());
        gameRecord.setStoreName(store.getName());
        gameRecord.setVendorId(vendor.getId());
        gameRecord.setVendorName(vendor.getName());
        gameRecord.setContactInfo(vendor.getContactInfo());
        gameRecord.setPoolTableId(byStoreUid.getId());
        gameRecord.setPoolTableName(byStoreUid.getTableNumber());
        gameRecordRepository.save(gameRecord);
        // ➡️ 建立 GameOrder
        GameOrder gameOrder = new GameOrder();
        gameOrder.setGameId(gameRecord.getGameId());
        gameOrder.setUserId(byUid.getUid());
        gameOrder.setTotalPrice(bookDeposit);
        gameOrder.setStartTime(startOfDay);
        gameOrder.setEndTime(endTime);
        gameOrder.setDuration(Duration.between(gameReq.getStartTime(), gameReq.getEndTime()).toHours());
        gameOrder.setStatus("IS_PAY");
        gameOrder.setPoolTableUid(byStoreUid.getUid());
        gameOrderRepository.save(gameOrder);

        return gameRecord;
    }



    @Transactional
    public void cancelBook(GameReq gameReq) throws Exception {
        // 取得目前登入用戶
        User byUid = userRepository.findById(SecurityUtils.getSecurityUser().getId())
                .orElseThrow(() -> new Exception("無法取得使用者資訊"));

        // 查詢該筆預約
        GameRecord gameRecord = gameRecordRepository.findByGameId(gameReq.getGameId());

        Store store = storeRepository.findById(gameRecord.getStoreId()).get();

        // 確認狀態為 "BOOK"
        if (!"BOOK".equals(gameRecord.getStatus())) {
            throw new Exception("此筆預約已無效或非預約狀態");
        }

        // 取得目前時間和預約時間
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime scheduledStartTime = gameRecord.getStartTime();
        if (scheduledStartTime == null) {
            throw new Exception("預約時間無效");
        }

        // 計算剩餘時間（分鐘）
        long minutesUntilStart = Duration.between(now, scheduledStartTime).toMinutes();
        Integer cancelBookTime = store.getCancelBookTime();
        int refundAmount = 0;
        if (minutesUntilStart > cancelBookTime) {
            // 超過設定時間，退還全部訂金
            refundAmount = gameRecord.getPrice();
            byUid.setAmount(byUid.getAmount() + refundAmount);
            userRepository.save(byUid);
        }

        // 更新紀錄狀態為 "CANCEL"
        gameRecord.setStatus("CANCEL");
        gameRecordRepository.save(gameRecord);
        PoolTable poolTable = poolTableRepository.findById(gameRecord.getPoolTableId()).get();
        LocalDateTime zonedStartTime = LocalDateTime.now();
        // 創建一筆取消訂單
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(byUid.getUid());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setTotalPrice(refundAmount); // 退款金額
        gameOrder.setStartTime(zonedStartTime);
        gameOrder.setEndTime(zonedStartTime);
        gameOrder.setDuration(0L);
        gameOrder.setStatus("CANCEL");
        gameOrder.setPoolTableUid(poolTable.getUid());

        gameOrderRepository.save(gameOrder); // 儲存取消訂單
    }

    public List<Map<String, Object>> getAvailableTimesByTableUid(String poolTableUid, LocalDate bookingDate) {
        int stepHours = 1;

        PoolTable poolTable = poolTableRepository.findByUid(poolTableUid)
                .orElseThrow(() -> new RuntimeException("找不到對應的桌台"));
        Long poolTableId = poolTable.getId();
        Long storeId = poolTable.getStore().getId();

        StorePricingSchedule schedule = storePricingScheduleRepository
                .findScheduleWithMergedTimeSlots(storeId, bookingDate.getDayOfWeek().toString())
                .orElseThrow(() -> new RuntimeException("未找到對應日期的時段"));

        // 查出所有當日該桌台的訂單（含保護期）
        List<GameOrder> allOrders = gameOrderRepository.findByGameIdAndStartTimeBetweenWithBuffer(
                storeId, poolTableId, bookingDate.atStartOfDay(), bookingDate.atTime(LocalTime.MAX)
        );

        List<Map<String, Object>> available = new ArrayList<>();

        for (TimeSlot slot : schedule.getTimeSlots()) {
            LocalTime segmentStart = slot.getStartTime();
            LocalTime segmentEnd = slot.getEndTime();
            boolean isDiscount = slot.getIsDiscount();
            int rate = isDiscount ? 80 : 100;

            while (!segmentStart.plusHours(stepHours).isAfter(segmentEnd)) {
                LocalTime subStart = segmentStart;
                LocalTime subEnd = subStart.plusHours(stepHours);

                LocalDateTime slotStart = bookingDate.atTime(subStart);
                LocalDateTime slotEnd = bookingDate.atTime(subEnd);

                // 檢查是否與任何訂單有衝突
                boolean isConflict = allOrders.stream().anyMatch(order -> {
                    LocalDateTime blockedStart = order.getStartTime().minusHours(1);
                    LocalDateTime blockedEnd = order.getEndTime().plusHours(1);
                    return slotStart.isBefore(blockedEnd) && slotEnd.isAfter(blockedStart);
                });

                if (!isConflict) {
                    Map<String, Object> timeMap = new HashMap<>();
                    timeMap.put("start", subStart.toString());
                    timeMap.put("end", subEnd.toString());
                    timeMap.put("rate", rate);
                    timeMap.put("isDiscount", isDiscount);
                    available.add(timeMap);
                }

                segmentStart = subEnd;
            }
        }

        return available;
    }







}
