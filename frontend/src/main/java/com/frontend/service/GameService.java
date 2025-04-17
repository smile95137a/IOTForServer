package com.frontend.service;

import com.frontend.entity.game.BookGame;
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
import com.frontend.res.game.GamePriceRes;
import com.frontend.res.game.GameRes;
import com.frontend.res.game.GameResponse;
import com.frontend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vo.GameVO;

import java.time.*;
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

    @Autowired
    private BookGameRepository bookGameRepository;
    @Autowired
    private TimeSlotRepository timeSlotRepository;

    public GameRecord bookStartGame(GameReq gameReq) throws Exception {
        // 查詢用戶
        PoolTable byStoreUid = poolTableRepository.findById(gameReq.getPoolTableId()).get();
        BookGame bookGame = bookGameRepository.findByGameId(gameReq.getGameId());
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

        bookGame.setStatus("COMPLETE");
        bookGameRepository.save(bookGame);

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
        gameRecord.setHint(store.getHint());
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


        GameRes gameRes = new GameRes(gameRecord , message , endTimeMinutes , vendor);


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


    @Transactional
    public GameResponse endGame(GameReq gameReq, Long id) throws Exception {
        // 取得遊戲紀錄
        GameRecord gameRecord = gameRecordRepository.findByGameId(gameReq.getGameId());
        if (gameRecord == null) {
            throw new Exception("找不到遊戲紀錄");
        }

        BookGame bookGame = bookGameRepository.findByGameId(gameReq.getGameId());

        // 取得結束時間
        LocalDateTime endDateTime = (bookGame == null) ? LocalDateTime.now() : bookGame.getEndTime();

        // 驗證結束時間
        if (endDateTime.isBefore(gameRecord.getStartTime())) {
            throw new Exception("結束時間不能小於開始時間");
        }

        // 取得店家資訊
        Store store = storeRepository.findById(gameRecord.getStoreId())
                .orElseThrow(() -> new Exception("店家信息未找到"));

        // 計算遊玩時間
        Duration duration = Duration.between(gameRecord.getStartTime(), endDateTime);
        long totalMinutes = duration.toMinutes();

        // 退還押金
        User user = userRepository.findById(id)
                .orElseThrow(() -> new Exception("用户信息未找到"));
        user.setAmount(user.getAmount() + store.getDeposit());
        userRepository.save(user);

        // 計算價格
        int adjustedPrice = calculateAdjustedPrice(store.getId(), gameRecord.getStartTime(), endDateTime);
        System.out.println("計算出的價格: " + adjustedPrice);

        // 取得桌台資訊
        PoolTable poolTable = poolTableRepository.findById(gameRecord.getPoolTableId())
                .orElseThrow(() -> new Exception("桌台信息未找到"));
        String newGameId = UUID.randomUUID().toString();

        // 建立遊戲訂單
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(user.getUid());
        if (bookGame == null) {
            gameOrder.setGameId(newGameId);
        } else {
            gameOrder.setGameId(gameReq.getGameId());
        }
        gameOrder.setTotalPrice(adjustedPrice);
        gameOrder.setStartTime(gameRecord.getStartTime());
        gameOrder.setEndTime(endDateTime);
        gameOrder.setDuration(totalMinutes);
        gameOrder.setStatus("IS_PAY");
        gameOrder.setPoolTableUid(poolTable.getUid());
        gameOrderRepository.save(gameOrder);

        // 更新桌台和遊戲紀錄
        poolTable.setIsUse(false);
        poolTableRepository.save(poolTable);

        gameRecord.setPrice(adjustedPrice);
        gameRecord.setStatus("ENDED");
        gameRecordRepository.save(gameRecord);

        // 建立回應
        GameResponse response = new GameResponse();
        response.setTotalSeconds(duration.toSeconds());
        response.setTotalPrice(adjustedPrice);
        response.setGameId(newGameId);

        return response;
    }

    private int calculateAdjustedPrice(Long storeId, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        double totalPrice = 0;
        long totalDiscountMinutes = 0;
        long totalRegularMinutes = 0;
        long totalEffectiveMinutes = 0;

        // 原始總分鐘（包含非營業時段），這邊也做進位
        long totalRawMinutes = adjustMinutes(Duration.between(startTime, endTime));

        LocalDateTime currentStart = startTime;

        while (currentStart.isBefore(endTime)) {
            LocalDateTime nextDay = currentStart.toLocalDate().plusDays(1).atStartOfDay();
            LocalDateTime currentEnd = endTime.isBefore(nextDay) ? endTime : nextDay;

            String dayOfWeek = currentStart.getDayOfWeek().toString().toLowerCase();
            StorePricingSchedule schedule = findScheduleForDay(storeId, dayOfWeek);

            LocalTime openTime = schedule.getOpenTime();
            LocalTime closeTime = schedule.getCloseTime();

            boolean is24HourOperation = false;
            if (openTime.equals(LocalTime.of(0, 0)) &&
                    (closeTime.equals(LocalTime.of(23, 59)) || closeTime.equals(LocalTime.of(23, 59, 59)))) {
                is24HourOperation = true;
            }
            if (openTime.equals(closeTime)) {
                is24HourOperation = true;
            }

            System.out.println("日期: " + currentStart.toLocalDate() + ", 是否24小時營業: " + is24HourOperation);
            System.out.println("營業時間: " + openTime + " - " + closeTime);

            LocalDateTime effectiveStart = currentStart;
            LocalDateTime effectiveEnd = currentEnd;

            if (!is24HourOperation) {
                if (effectiveStart.toLocalTime().isBefore(openTime)) {
                    effectiveStart = effectiveStart.toLocalDate().atTime(openTime);
                }
                if (effectiveEnd.toLocalTime().isAfter(closeTime)) {
                    effectiveEnd = effectiveEnd.toLocalDate().atTime(closeTime);
                }
            }

            if (effectiveStart.isBefore(effectiveEnd)) {
                long effectiveMinutes = adjustMinutes(Duration.between(effectiveStart, effectiveEnd));
                totalEffectiveMinutes += effectiveMinutes;

                System.out.println("當日有效時間: " + effectiveMinutes + " 分鐘");

                TimeSlot discountSlot = schedule.getTimeSlots().stream()
                        .filter(TimeSlot::getIsDiscount)
                        .findFirst()
                        .orElse(null);

                long discountMinutes = 0;
                if (discountSlot != null) {
                    LocalTime discountStart = discountSlot.getStartTime();
                    LocalTime discountEnd = discountSlot.getEndTime();

                    LocalDateTime discSlotStart = effectiveStart.toLocalDate().atTime(discountStart);
                    LocalDateTime discSlotEnd = effectiveStart.toLocalDate().atTime(discountEnd);

                    if (discountEnd.isBefore(discountStart)) {
                        discSlotEnd = effectiveStart.toLocalDate().plusDays(1).atTime(discountEnd);
                    }

                    if (discSlotStart.isBefore(effectiveEnd) && discSlotEnd.isAfter(effectiveStart)) {
                        LocalDateTime overlapStart = discSlotStart.isAfter(effectiveStart) ? discSlotStart : effectiveStart;
                        LocalDateTime overlapEnd = discSlotEnd.isBefore(effectiveEnd) ? discSlotEnd : effectiveEnd;

                        discountMinutes = adjustMinutes(Duration.between(overlapStart, overlapEnd));
                    }
                }

                long regularMinutes = effectiveMinutes - discountMinutes;

                double discountRate = schedule.getDiscountRate();
                double regularRate = schedule.getRegularRate();

                double discountPrice = discountMinutes * discountRate;
                double regularPrice = regularMinutes * regularRate;

                totalPrice += discountPrice + regularPrice;
                totalDiscountMinutes += discountMinutes;
                totalRegularMinutes += regularMinutes;

                System.out.println("當日優惠時段: " + discountMinutes + " 分鐘, 一般時段: " + regularMinutes + " 分鐘");
                System.out.println("當日金額: " + (discountPrice + regularPrice));
            }

            currentStart = currentEnd;
        }

        System.out.println("計算結果:");
        System.out.println("總共遊玩時間(原始): " + totalRawMinutes + " 分鐘");
        System.out.println("總有效遊玩時間: " + totalEffectiveMinutes + " 分鐘");
        System.out.println("總優惠時段: " + totalDiscountMinutes + " 分鐘");
        System.out.println("總一般時段: " + totalRegularMinutes + " 分鐘");
        System.out.println("總金額: " + totalPrice);

        return (int) Math.round(totalPrice);
    }

    /**
     * 將秒數無條件進位成整分鐘
     */
    private long adjustMinutes(Duration duration) {
        long seconds = duration.getSeconds();
        return (seconds + 59) / 60;
    }


    private StorePricingSchedule findScheduleForDay(Long storeId, String dayOfWeek) throws Exception {
        // 根據店鋪 ID 查找對應的時段設置
        List<StorePricingSchedule> schedules = storePricingScheduleRepository.findByStoreId(storeId);
        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
                .findFirst()
                .orElseThrow(() -> new Exception("找不到 " + dayOfWeek + " 的時段設定"));
    }



    @Transactional
    public GameRes checkout(CheckoutReq checkoutReq , Long id) throws Exception {

        if(checkoutReq.getGameId() == null){
            PoolTable poolTable = poolTableRepository.findById(checkoutReq.getPoolTableId()).get();
            GameRecord gameRecord = gameRecordRepository.findByPoolTableIdAndStatus(poolTable.getId() , "STARTED");
            checkoutReq.setGameId(gameRecord.getGameId());
        }

        // 获取当前用户
        User user = userRepository.findById(id).get(); // 假设有一个获取当前用户的方式

        GameRecord byGameId = gameRecordRepository.findByGameId(checkoutReq.getGameId());
        Optional<PoolTable> byId = poolTableRepository.findById(byGameId.getPoolTableId());
        // 根据支付类型进行判断
        GameReq gameReq = new GameReq();
        gameReq.setGameId(checkoutReq.getGameId());
        gameReq.setPoolTableUId(byId.get().getUid());
        GameResponse gameResponse = this.endGame(gameReq, SecurityUtils.getSecurityUser().getId());
        Integer totalPrice = (int)gameResponse.getTotalPrice();

        String gameId = gameResponse.getGameId();
        System.out.println("user" + user.getAmount());
        System.out.println("total" + totalPrice);
        switch (checkoutReq.getPayType()) {
            case "1": // 儲值金支付
                int remainingAmount = totalPrice;

// 計算可用餘額（儲值金額 + 額外金額）
                int availableBalance = user.getAmount() + user.getPoint();

// 檢查可用餘額是否足夠
                if (availableBalance >= remainingAmount) {
                    // 儲值金額足夠
                    if (user.getAmount() >= remainingAmount) {
                        user.setAmount((int) (user.getAmount() - remainingAmount));
                        remainingAmount = 0;
                    } else {
                        // 儲值金額不足，扣光它，剩下的再從額外金額扣
                        remainingAmount -= user.getAmount();
                        user.setAmount(0);

                        user.setPoint((int) (user.getPoint() - remainingAmount));
                        remainingAmount = 0;
                    }
                } else {
                    // 餘額不足
                    throw new RuntimeException("儲值金額和額外獎勳不足以支付總金額");
                }
                availableBalance = user.getAmount() + user.getPoint();
                user.setBalance((int) availableBalance);
                // 儲值金扣除後保存更新后的用戶數據
                userRepository.save(user);
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
        GameOrder game = gameOrderRepository.findByGameId(gameId);
        PoolTable poolTable = poolTableRepository.findById(checkoutReq.getPoolTableId()).get();
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

        return new GameRes(null , null , 0L , vendor);
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

        BookGame bookGame = new BookGame();
        bookGame.setUserUId(byUid.getUid());
        bookGame.setGameId(gameRecord.getGameId());
        bookGame.setStartTime(startOfDay);
        bookGame.setEndTime(endTime);
        bookGame.setStatus("BOOK");
        bookGame.setStoreId(store.getId());
        bookGame.setStoreName(store.getName());
        bookGame.setVendorId(vendor.getId());
        bookGame.setVendorName(vendor.getName());
        bookGame.setContactInfo(vendor.getContactInfo());
        bookGame.setPoolTableId(byStoreUid.getId());
        bookGame.setPoolTableName(byStoreUid.getTableNumber());
        bookGameRepository.save(bookGame);
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

        BookGame byGameId = bookGameRepository.findByGameId(gameRecord.getGameId());
        byGameId.setStatus("CANCEL");
        bookGameRepository.save(byGameId);

        gameOrderRepository.save(gameOrder); // 儲存取消訂單
    }

    @Transactional
    public Map<String, List<Map<String, Object>>> getAvailableTimes(Long storeId, LocalDate bookingDate, Long poolTableId) {
        int duration = 1; // 每個時段的長度為1小時
        int maxSlots = 24; // 限制為24小時內的時段，避免無限循環

        // 取得店家營業時段
        StorePricingSchedule schedule = storePricingScheduleRepository.findByStoreId(storeId)
                .stream()
                .filter(s -> s.getDayOfWeek().equalsIgnoreCase(bookingDate.getDayOfWeek().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到對應日期的時段"));

        // 取得對應的桌台
        PoolTable poolTable = poolTableRepository.findById(poolTableId)
                .orElseThrow(() -> new RuntimeException("未找到指定桌台"));

        // 取得 timeSlots 列表
        List<TimeSlot> timeSlots = schedule.getTimeSlots();

        List<Object[]> results = gameRecordRepository.findGameIdsByStoreIdStatusAndPoolTableId(storeId, poolTableId);

        List<GameVO> gameVOList = results.stream()
                .map(result -> new GameVO((String) result[0], (String) result[1]))
                .collect(Collectors.toList());

        List<GameVO> bookedGames = gameVOList.stream()
                .filter(x -> "BOOK".equals(x.getStatus()))
                .collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> availableTimesMap = new HashMap<>();
        List<Map<String, Object>> availableTimes = new ArrayList<>();

        // 檢查是否為24小時營業
        boolean is24HoursOperation = schedule.getOpenTime().equals(schedule.getCloseTime());

        if (bookedGames.isEmpty()) {
            // 沒有預約的情況，直接計算可用時段
            calculateAvailableTimeSlots(schedule.getOpenTime(), schedule.getCloseTime(),
                    duration, maxSlots, schedule, timeSlots, availableTimes, is24HoursOperation);
        } else {
            // 預約存在，過濾和考慮預約衝突
            List<String> gameIds = bookedGames.stream()
                    .map(GameVO::getGameId)
                    .collect(Collectors.toList());

            List<GameOrder> allBookings = gameOrderRepository.findByGameIds(gameIds);
            LocalDateTime startOfDay = bookingDate.atStartOfDay(); // 當天 00:00:00
            LocalDateTime endOfDay = bookingDate.atTime(LocalTime.MAX); // 當天 23:59:59.999999999

            // 取得當天所有可能影響時段的預約
            List<GameOrder> relevantBookings = allBookings.stream()
                    .filter(order -> order.getStartTime() != null && order.getEndTime() != null) // 避免 NullPointerException
                    .filter(order -> {
                        LocalDateTime extendedStart = order.getStartTime().minusHours(1);
                        LocalDateTime extendedEnd = order.getEndTime().plusHours(1);
                        // 只要這個訂單在當天的範圍內影響時段，就納入計算
                        return !(extendedEnd.isBefore(startOfDay) || extendedStart.isAfter(endOfDay));
                    })
                    .collect(Collectors.toList());

            if (relevantBookings.isEmpty()) {
                calculateAvailableTimeSlots(schedule.getOpenTime(), schedule.getCloseTime(),
                        duration, maxSlots, schedule, timeSlots, availableTimes, is24HoursOperation);
            } else {
                calculateAvailableTimeSlotsWithBookings(schedule.getOpenTime(), schedule.getCloseTime(),
                        duration, maxSlots, bookingDate, relevantBookings, schedule, timeSlots,
                        availableTimes, is24HoursOperation);
            }
        }

        availableTimesMap.put(String.valueOf(poolTableId), availableTimes);
        return availableTimesMap;
    }

    // 計算所有可用時段（無預約衝突時）
    private static void calculateAvailableTimeSlots(LocalTime openTime, LocalTime closeTime,
                                                    int duration, int maxSlots,
                                                    StorePricingSchedule schedule,
                                                    List<TimeSlot> timeSlots,
                                                    List<Map<String, Object>> availableTimes,
                                                    boolean is24HoursOperation) {
        LocalTime startTime = openTime;
        int slotCount = 0;

        // 如果是24小時營業，設定closeTime為明天的openTime (等同於一整天)
        LocalTime effectiveCloseTime = is24HoursOperation ? openTime.plusHours(24) : closeTime;
        LocalTime dayEndTime = LocalTime.of(23, 59, 59);

        while (slotCount < maxSlots) {
            LocalTime slotEndTime = startTime.plusMinutes(duration * 60);

            // 如果是24小時營業，確保不超過當天結束
            if (is24HoursOperation && slotEndTime.isAfter(dayEndTime)) {
                break;
            }

            // 一般情況，確保不超過營業結束時間
            if (!is24HoursOperation && slotEndTime.isAfter(closeTime)) {
                slotEndTime = closeTime;
            }

            // 如果已達營業結束時間，跳出循環
            if (startTime.equals(effectiveCloseTime) ||
                    (is24HoursOperation && startTime.equals(dayEndTime))) {
                break;
            }

            int rate = getRateForTime(timeSlots, schedule, startTime);

            Map<String, Object> availableTimeSlot = new HashMap<>();
            availableTimeSlot.put("start", startTime.toString());
            availableTimeSlot.put("end", slotEndTime.toString());
            availableTimeSlot.put("rate", rate);
            availableTimes.add(availableTimeSlot);

            startTime = slotEndTime;
            slotCount++;

            // 如果已達營業結束時間或24小時店已到一天結束，跳出循環
            if ((!is24HoursOperation && startTime.equals(closeTime)) ||
                    (is24HoursOperation && startTime.isAfter(dayEndTime))) {
                break;
            }
        }
    }

    // 計算有預約衝突時的可用時段
    private static void calculateAvailableTimeSlotsWithBookings(LocalTime openTime, LocalTime closeTime,
                                                                int duration, int maxSlots, LocalDate bookingDate,
                                                                List<GameOrder> relevantBookings,
                                                                StorePricingSchedule schedule,
                                                                List<TimeSlot> timeSlots,
                                                                List<Map<String, Object>> availableTimes,
                                                                boolean is24HoursOperation) {
        LocalTime startTime = openTime;
        int slotCount = 0;

        // 如果是24小時營業，設定closeTime為明天的openTime (等同於一整天)
        LocalTime effectiveCloseTime = is24HoursOperation ? openTime.plusHours(24) : closeTime;
        LocalTime dayEndTime = LocalTime.of(23, 59, 59);

        while (slotCount < maxSlots) {
            LocalTime slotEndTime = startTime.plusMinutes(duration * 60);

            // 如果是24小時營業，確保不超過當天結束
            if (is24HoursOperation && slotEndTime.isAfter(dayEndTime)) {
                break;
            }

            // 一般情況，確保不超過營業結束時間
            if (!is24HoursOperation && slotEndTime.isAfter(closeTime)) {
                slotEndTime = closeTime;
            }

            // 如果已達營業結束時間，跳出循環
            if (startTime.equals(effectiveCloseTime) ||
                    (is24HoursOperation && startTime.equals(dayEndTime))) {
                break;
            }

            // 轉換為當天的 LocalDateTime 以便比較
            LocalDateTime slotStartDateTime = bookingDate.atTime(startTime);
            LocalDateTime slotEndDateTime = bookingDate.atTime(slotEndTime);

            boolean isConflict = relevantBookings.stream()
                    .anyMatch(order -> {
                        // 預訂時段的前後各加1小時緩衝
                        LocalDateTime orderStart = order.getStartTime().minusHours(1);
                        LocalDateTime orderEnd = order.getEndTime().plusHours(1);

                        // 檢查時段是否衝突 (四種重疊情況)
                        return (slotStartDateTime.isBefore(orderEnd) &&
                                slotEndDateTime.isAfter(orderStart));
                    });

            if (!isConflict) {
                int rate = getRateForTime(timeSlots, schedule, startTime);

                Map<String, Object> availableTimeSlot = new HashMap<>();
                availableTimeSlot.put("start", startTime.toString());
                availableTimeSlot.put("end", slotEndTime.toString());
                availableTimeSlot.put("rate", rate);
                availableTimes.add(availableTimeSlot);
            }

            startTime = slotEndTime;
            slotCount++;

            // 如果已達營業結束時間或24小時店已到一天結束，跳出循環
            if ((!is24HoursOperation && startTime.equals(closeTime)) ||
                    (is24HoursOperation && startTime.isAfter(dayEndTime))) {
                break;
            }
        }
    }

    private static int getRateForTime(List<TimeSlot> timeSlots, StorePricingSchedule schedule, LocalTime startTime) {
        for (TimeSlot slot : timeSlots) {
            if (!startTime.isBefore(slot.getStartTime()) && startTime.isBefore(slot.getEndTime())) {
                return slot.getIsDiscount() ? schedule.getRegularRate() : schedule.getDiscountRate();
            }
        }
        return schedule.getRegularRate(); // 預設回傳 regularRate
    }

    public List<BookGame> getBookGame() {
        Optional<User> byId = userRepository.findById(SecurityUtils.getSecurityUser().getId());
        return bookGameRepository.findByUserUId(byId.get().getUid());
    }

    public GamePriceRes getGamePrice(GameReq gameReq) {
        LocalDateTime end = LocalDateTime.now();
        LocalDate localDate = LocalDate.now();
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());
        LocalDateTime startTime = byGameId.getStartTime();

        StorePricingSchedule schedule = storePricingScheduleRepository.findByStoreId(byGameId.getStoreId())
                .stream()
                .filter(s -> s.getDayOfWeek().equalsIgnoreCase(localDate.getDayOfWeek().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到對應日期的時段"));

        List<TimeSlot> timeSlots = schedule.getTimeSlots();
        double totalAmount = 0;

        // 🟢 計算總分鐘數
        long totalSeconds = ChronoUnit.SECONDS.between(startTime, end);
        long totalMinutes = (totalSeconds + 59) / 60; // 四捨五入到整分鐘

        LocalTime currentTime = startTime.toLocalTime();
        while (!currentTime.isAfter(end.toLocalTime())) {
            LocalTime finalCurrentTime = currentTime;
            TimeSlot applicableSlot = timeSlots.stream()
                    .filter(slot -> !finalCurrentTime.isBefore(slot.getStartTime()) && finalCurrentTime.isBefore(slot.getEndTime()))
                    .findFirst()
                    .orElse(null);

            double rate = (applicableSlot != null && applicableSlot.getIsDiscount())
                    ? schedule.getDiscountRate()
                    : schedule.getRegularRate();

            totalAmount += rate; // 🟢 以分鐘為單位計算
            currentTime = currentTime.plusMinutes(1);

        }
// 🟢 無條件進位
        totalAmount = Math.ceil(totalAmount);
        // 🟢 回傳秒數 + 計算好的金額
        return new GamePriceRes(totalAmount, totalSeconds);
    }

}
