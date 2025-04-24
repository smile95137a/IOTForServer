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
        boolean b = this.checkoutOrder();
        if(b){
            throw new Exception("有尚未結帳的球局，請先結帳後才能使用開台服務");
        }
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

        boolean b = this.checkoutOrder();
        if(b){
            throw new Exception("有尚未結帳的球局，請先結帳後才能使用開台服務");
        }

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

        LocalTime nowTime = LocalTime.now();
        LocalTime openTime = currentSchedule.getOpenTime();
        LocalTime closeTime = currentSchedule.getCloseTime();

        if (nowTime.isBefore(openTime) || nowTime.isAfter(closeTime)) {
            throw new Exception("非營業時間，無法開台。營業時間為：" + openTime + " - " + closeTime);
        }

        // 计算价格
        int regularRateAmount = currentSchedule.getRegularRate();
        int discountRateAmount = currentSchedule.getDiscountRate();

        // 扣除押金
        int remainingAmount = store.getDeposit();
        int availableBalance = byUid.getAmount() + byUid.getPoint();
        if (availableBalance >= store.getDeposit()) {
            // 儲值金額足夠
            if (byUid.getAmount() >= store.getDeposit()) {
                byUid.setAmount((int) (byUid.getAmount() - remainingAmount));
                remainingAmount = 0;
            } else {
                // 儲值金額不足，扣光它，剩下的再從額外金額扣
                remainingAmount -= byUid.getAmount();
                byUid.setAmount(0);

                byUid.setPoint((int) (byUid.getPoint() - remainingAmount));
                remainingAmount = 0;
            }
        } else {
            // 餘額不足
            throw new RuntimeException("儲值金額和額外獎勳不足以支付總金額");
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
        long totalNonBusinessHoursMinutes = 0;

        // 原始總分鐘，使用調整後的計時規則
        long totalRawMinutes = adjustMinutes(Duration.between(startTime, endTime));

        LocalDateTime currentStart = startTime;

        while (currentStart.isBefore(endTime)) {
            LocalDateTime nextDay = currentStart.toLocalDate().plusDays(1).atStartOfDay();
            LocalDateTime currentEnd = endTime.isBefore(nextDay) ? endTime : nextDay;

            String dayOfWeek = currentStart.getDayOfWeek().toString().toLowerCase();
            StorePricingSchedule schedule = findScheduleForDay(storeId, dayOfWeek);

            LocalTime openTime = schedule.getOpenTime();
            LocalTime closeTime = schedule.getCloseTime();

            // 檢查是否24小時營業
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

            // 計算當日總時間（不考慮營業時間限制）
            long totalDayMinutes = adjustMinutes(Duration.between(currentStart, currentEnd));

            // 計算營業時間內的時間
            LocalDateTime businessStart = currentStart;
            LocalDateTime businessEnd = currentEnd;

            // 非24小時營業時，計算營業時間與非營業時間
            if (!is24HourOperation) {
                // 創建營業開始和結束的日期時間
                LocalDateTime dayOpenTime = currentStart.toLocalDate().atTime(openTime);
                LocalDateTime dayCloseTime = currentStart.toLocalDate().atTime(closeTime);

                // 處理跨日的情況（例如營業至凌晨）
                if (closeTime.isBefore(openTime)) {
                    dayCloseTime = dayCloseTime.plusDays(1);
                }

                // 計算營業時間內的時段
                if (currentEnd.isBefore(dayOpenTime) || currentStart.isAfter(dayCloseTime)) {
                    // 完全在非營業時間內
                    businessStart = null;
                    businessEnd = null;
                } else {
                    // 部分或全部在營業時間內
                    if (currentStart.isBefore(dayOpenTime)) {
                        businessStart = dayOpenTime;
                    }
                    if (currentEnd.isAfter(dayCloseTime)) {
                        businessEnd = dayCloseTime;
                    }
                }
            }

            // 處理營業時間內的費用計算
            long businessHoursMinutes = 0;
            if (businessStart != null && businessEnd != null && businessStart.isBefore(businessEnd)) {
                businessHoursMinutes = adjustMinutes(Duration.between(businessStart, businessEnd));

                // 計算優惠時段
                TimeSlot discountSlot = schedule.getTimeSlots().stream()
                        .filter(TimeSlot::getIsDiscount)
                        .findFirst()
                        .orElse(null);

                long discountMinutes = 0;
                if (discountSlot != null) {
                    LocalTime discountStart = discountSlot.getStartTime();
                    LocalTime discountEnd = discountSlot.getEndTime();

                    LocalDateTime discSlotStart = businessStart.toLocalDate().atTime(discountStart);
                    LocalDateTime discSlotEnd = businessStart.toLocalDate().atTime(discountEnd);

                    if (discountEnd.isBefore(discountStart)) {
                        discSlotEnd = discSlotEnd.plusDays(1);
                    }

                    if (discSlotStart.isBefore(businessEnd) && discSlotEnd.isAfter(businessStart)) {
                        LocalDateTime overlapStart = discSlotStart.isAfter(businessStart) ? discSlotStart : businessStart;
                        LocalDateTime overlapEnd = discSlotEnd.isBefore(businessEnd) ? discSlotEnd : businessEnd;

                        discountMinutes = adjustMinutes(Duration.between(overlapStart, overlapEnd));
                    }
                }

                long regularMinutes = businessHoursMinutes - discountMinutes;

                double discountRate = schedule.getDiscountRate();
                double regularRate = schedule.getRegularRate();

                double discountPrice = discountMinutes * discountRate;
                double regularPrice = regularMinutes * regularRate;

                totalPrice += discountPrice + regularPrice;
                totalDiscountMinutes += discountMinutes;
                totalRegularMinutes += regularMinutes;
                totalEffectiveMinutes += businessHoursMinutes;

                System.out.println("營業時間內: 優惠時段: " + discountMinutes + " 分鐘, 一般時段: " + regularMinutes + " 分鐘");
                System.out.println("營業時間內金額: " + (discountPrice + regularPrice));
            }

            // 計算非營業時間的費用
            long nonBusinessHoursMinutes = totalDayMinutes - businessHoursMinutes;
            if (nonBusinessHoursMinutes > 0) {
                double regularRate = schedule.getRegularRate();
                double nonBusinessPrice = nonBusinessHoursMinutes * regularRate;

                totalPrice += nonBusinessPrice;
                totalRegularMinutes += nonBusinessHoursMinutes;
                totalNonBusinessHoursMinutes += nonBusinessHoursMinutes;
                totalEffectiveMinutes += nonBusinessHoursMinutes;

                System.out.println("非營業時間: " + nonBusinessHoursMinutes + " 分鐘");
                System.out.println("非營業時間金額: " + nonBusinessPrice);
            }

            currentStart = currentEnd;
        }

        System.out.println("計算結果:");
        System.out.println("總共遊玩時間(原始): " + totalRawMinutes + " 分鐘");
        System.out.println("總有效遊玩時間: " + totalEffectiveMinutes + " 分鐘");
        System.out.println("總優惠時段: " + totalDiscountMinutes + " 分鐘");
        System.out.println("總一般時段: " + totalRegularMinutes + " 分鐘");
        System.out.println("總非營業時間: " + totalNonBusinessHoursMinutes + " 分鐘");
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
    /**
     * 計算實際要收費的分鐘數
     * - 第一段：不滿 1 分鐘也要當 1 分鐘
     * - 其餘：最後不足 1 分鐘部分若超過 30 秒才進位
     */
    private long adjustMinutesForPrice(Duration duration, boolean isFirstSegment) {
        long seconds = duration.getSeconds();
        if (seconds <= 0) return 0;

        if (isFirstSegment) {
            return 1;
        }

        long minutes = seconds / 60;
        long remainingSeconds = seconds % 60;

        return minutes + (remainingSeconds > 30 ? 1 : 0);
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
        long durationHours = Duration.between(gameReq.getStartTime(), gameReq.getEndTime()).toHours();
        if (durationHours <= 0) {
            throw new Exception("預約時間必須至少為1小時");
        }
        int bookDeposit = (int) (store.getDeposit() * durationHours);

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
        int duration = 1;
        int maxSlots = 24;

        boolean isToday = bookingDate.equals(LocalDate.now());
        LocalTime now = LocalTime.now();

        StorePricingSchedule schedule = storePricingScheduleRepository.findByStoreId(storeId)
                .stream()
                .filter(s -> s.getDayOfWeek().equalsIgnoreCase(bookingDate.getDayOfWeek().toString()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("未找到對應日期的時段"));

        PoolTable poolTable = poolTableRepository.findById(poolTableId)
                .orElseThrow(() -> new RuntimeException("未找到指定桌台"));

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

        // 處理營業時間
        LocalTime openTime = schedule.getOpenTime();
        LocalTime closeTime = schedule.getCloseTime();

        // 生成時段 (嚴格限制在當天的 00:00 到 23:59)
        List<LocalTime> availableStartTimes = generateTimeSlots(openTime, closeTime);

        // 如果是今天，過濾掉已經過去的時段
        if (isToday) {
            availableStartTimes = availableStartTimes.stream()
                    .filter(time -> time.isAfter(now))
                    .collect(Collectors.toList());
        }

        // 處理已預約的時段
        if (!bookedGames.isEmpty()) {
            List<String> gameIds = bookedGames.stream()
                    .map(GameVO::getGameId)
                    .collect(Collectors.toList());

            List<GameOrder> allBookings = gameOrderRepository.findByGameIds(gameIds);
            LocalDateTime startOfDay = bookingDate.atStartOfDay();
            LocalDateTime endOfDay = bookingDate.atTime(LocalTime.MAX);

            List<GameOrder> relevantBookings = allBookings.stream()
                    .filter(order -> order.getStartTime() != null && order.getEndTime() != null)
                    .filter(order -> {
                        LocalDateTime extendedStart = order.getStartTime().minusHours(1);
                        LocalDateTime extendedEnd = order.getEndTime().plusHours(1);
                        return !(extendedEnd.isBefore(startOfDay) || extendedStart.isAfter(endOfDay));
                    })
                    .collect(Collectors.toList());

            // 過濾掉已預約的時段
            if (!relevantBookings.isEmpty()) {
                availableStartTimes = filterBookedTimeSlots(availableStartTimes, relevantBookings, bookingDate, duration);
            }
        }

        // 轉換成前端所需的格式
        for (int i = 0; i < availableStartTimes.size(); i++) {
            LocalTime startTime = availableStartTimes.get(i);
            LocalTime endTime;

            // 處理23:00-00:00的特殊情況
            if (startTime.getHour() == 23) {
                endTime = LocalTime.of(0, 0);
            } else {
                endTime = startTime.plusHours(duration);
            }

            int rate = getRateForTime(timeSlots, schedule, startTime);

            Map<String, Object> availableTimeSlot = new HashMap<>();
            availableTimeSlot.put("start", startTime.toString());
            availableTimeSlot.put("end", endTime.toString());
            availableTimeSlot.put("rate", rate);
            availableTimes.add(availableTimeSlot);
        }

        availableTimesMap.put(String.valueOf(poolTableId), availableTimes);
        return availableTimesMap;
    }

    // 生成當天的所有小時時段（嚴格在當天範圍內）
    private List<LocalTime> generateTimeSlots(LocalTime openTime, LocalTime closeTime) {
        List<LocalTime> timeSlots = new ArrayList<>();

        // 判斷是否為24小時營業或跨午夜營業
        boolean is24HoursOperation = openTime.equals(closeTime);
        boolean crossesMidnight = !is24HoursOperation && openTime.isAfter(closeTime);

        // 生成當天的所有小時時段
        for (int hour = 0; hour < 24; hour++) {
            LocalTime currentTime = LocalTime.of(hour, 0);

            if (is24HoursOperation) {
                // 24小時營業，添加所有時段
                timeSlots.add(currentTime);
            } else if (crossesMidnight) {
                // 跨午夜營業，例如從晚上10點到早上6點
                // 在這個情況下，只添加 openTime 到 23:59 和 00:00 到 closeTime 的時段
                if (!currentTime.isBefore(openTime) || !currentTime.isAfter(closeTime)) {
                    timeSlots.add(currentTime);
                }
            } else {
                // 正常營業時間，例如早上9點到晚上10點
                if (!currentTime.isBefore(openTime) && !currentTime.isAfter(closeTime.minusHours(1))) {
                    timeSlots.add(currentTime);
                }
            }
        }

        return timeSlots;
    }

    // 過濾掉已預約的時段
    private List<LocalTime> filterBookedTimeSlots(List<LocalTime> availableStartTimes,
                                                  List<GameOrder> relevantBookings,
                                                  LocalDate bookingDate,
                                                  int duration) {
        return availableStartTimes.stream()
                .filter(startTime -> {
                    LocalTime endTime = startTime.plusHours(duration);
                    LocalDateTime slotStartDateTime = bookingDate.atTime(startTime);

                    // 處理 23:00-00:00 的特殊情況
                    LocalDateTime slotEndDateTime;
                    if (startTime.getHour() == 23) {
                        slotEndDateTime = bookingDate.plusDays(1).atStartOfDay();
                    } else {
                        slotEndDateTime = bookingDate.atTime(endTime);
                    }

                    // 檢查此時段是否與任何預約衝突
                    return relevantBookings.stream()
                            .noneMatch(order -> {
                                LocalDateTime orderStart = order.getStartTime().minusHours(1);
                                LocalDateTime orderEnd = order.getEndTime().plusHours(1);
                                return (slotStartDateTime.isBefore(orderEnd) && slotEndDateTime.isAfter(orderStart));
                            });
                })
                .collect(Collectors.toList());
    }

    private static int getRateForTime(List<TimeSlot> timeSlots, StorePricingSchedule schedule, LocalTime startTime) {
        for (TimeSlot slot : timeSlots) {
            if (!startTime.isBefore(slot.getStartTime()) && startTime.isBefore(slot.getEndTime())) {
                return slot.getIsDiscount() ? schedule.getRegularRate() : schedule.getDiscountRate();
            }
        }
        return schedule.getRegularRate();
    }


    public List<BookGame> getBookGame() {
        Optional<User> byId = userRepository.findById(SecurityUtils.getSecurityUser().getId());
        return bookGameRepository.findByUserUId(byId.get().getUid());
    }

    public GamePriceRes getGamePrice(GameReq gameReq) {
        // 取得遊戲紀錄
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());
        if (byGameId == null) {
            throw new RuntimeException("找不到遊戲紀錄");
        }

        // 取得開始和結束時間
        LocalDateTime startTime = byGameId.getStartTime();
        LocalDateTime endTime = LocalDateTime.now();

        // 驗證結束時間
        if (endTime.isBefore(startTime)) {
            throw new RuntimeException("結束時間不能小於開始時間");
        }

        // 計算總秒數
        long totalSeconds = ChronoUnit.SECONDS.between(startTime, endTime);

        try {
            // 使用與endGame相同的價格計算邏輯
            int totalPrice = calculateAdjustedPrice(byGameId.getStoreId(), startTime, endTime);

            // 回傳計算結果
            return new GamePriceRes((double) totalPrice, totalSeconds);
        } catch (Exception e) {
            throw new RuntimeException("計算價格時發生錯誤: " + e.getMessage());
        }
    }

    private boolean checkoutOrder() {
        User user = userRepository.findById(SecurityUtils.getSecurityUser().getId()).get();
        List<GameRecord> unpaid = gameRecordRepository.findByUserUidAndStatus(user.getUid(), "UNPAID");
        for(GameRecord noPay : unpaid){
            if(noPay.getStatus().equals("UNPAID")){
                return true;
            }
        }
        return false;
    }

}
