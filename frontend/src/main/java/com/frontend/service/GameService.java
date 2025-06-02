package com.frontend.service;

import com.frontend.config.GameBookingException;
import com.frontend.entity.game.BookGame;
import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.*;
import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.game.BookGameReq;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.req.store.TimeSlotInfo;
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

        boolean c = this.checkPooltable(byStoreUid.getUid());
        if(c){
            throw new Exception("球局目前不開放使用，請換別桌進行球局");
        }

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
        gameRecord.setStartTime(now);
        gameRecordRepository.save(gameRecord);

        byGameId.setStartTime(now);
        gameOrderRepository.save(byGameId);

        bookGame.setStatus("COMPLETE");
        bookGameRepository.save(bookGame);

        // 開啟桌台使用
        byStoreUid.setIsUse(true);
        poolTableRepository.save(byStoreUid);

        return gameRecord;
    }

    public GameRes startGame(GameReq gameReq, Long id) throws Exception {
        boolean b = this.checkoutOrder();
        if(b){
            throw new Exception("有尚未結帳的球局，請先結帳後才能使用開台服務");
        }

        // 查詢用戶、桌台、店家、廠商
        User byUid = userRepository.findById(id).orElseThrow(() -> new Exception("找不到用戶"));
        PoolTable byStoreUid = poolTableRepository.findByUid(gameReq.getPoolTableUId()).orElseThrow(() -> new Exception("找不到桌台"));
        Store store = storeRepository.findById(byStoreUid.getStore().getId()).orElseThrow(() -> new Exception("找不到店家"));
        Vendor vendor = vendorRepository.findById(store.getVendor().getId()).orElseThrow(() -> new Exception("找不到廠商"));

        if (this.checkPooltable(byStoreUid.getUid())) {
            throw new Exception("球局目前不開放使用，請換別桌進行球局");
        }

        if (gameIsUse(byUid.getUid())) {
            throw new Exception("已經有開放中的球局");
        }

        // 檢查今天是否為特殊日期
        LocalDate today = LocalDate.now();
        Optional<SpecialDate> todaySpecialDate = getTodaySpecialDate(store, today);

        LocalTime openTime;
        LocalTime closeTime;
        Double regularRate;
        Double discountRate;
        StorePricingSchedule currentSchedule = null;

        if (todaySpecialDate.isPresent()) {
            // 使用特殊日期的營業時間和價格
            SpecialDate specialDate = todaySpecialDate.get();
            openTime = specialDate.getOpenTime();
            closeTime = specialDate.getCloseTime();
            regularRate = specialDate.getRegularRate();

            // 特殊日期可能沒有折扣價，使用相同價格
            discountRate = regularRate;
        } else {
            // 使用一般日期的營業時間和價格
            String currentDayString = today.getDayOfWeek().toString().toLowerCase();
            List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());

            currentSchedule = pricingSchedules.stream()
                    .filter(s -> s.getDayOfWeek().toLowerCase().equals(currentDayString))
                    .findFirst()
                    .orElseThrow(() -> new Exception("沒有找到當天的訊息"));

            openTime = currentSchedule.getOpenTime();
            closeTime = currentSchedule.getCloseTime();
            regularRate = currentSchedule.getRegularRate();
            discountRate = currentSchedule.getDiscountRate();
        }

        // 檢查是否在營業時間內
        LocalTime nowTime = LocalTime.now();
        if (nowTime.isBefore(openTime) || nowTime.isAfter(closeTime)) {
            throw new Exception("非營業時間，無法開台。營業時間為：" + openTime + " - " + closeTime);
        }

        // ✅ 判斷是否落在已預約時間的前後一小時內，若是，則需要 confirm=true
        List<String> bookedGames = gameRecordRepository.findGameIdByStoreIdAndStatus(store.getId(), "BOOKED");
        LocalDateTime now = LocalDateTime.now();
        boolean isInCriticalPeriod = false;

        for (String bookedGame : bookedGames) {
            GameOrder order = gameOrderRepository.findByGameId(bookedGame);
            if (order == null) continue;

            LocalDateTime bookedStartTime = order.getStartTime();
            LocalDateTime oneHourBefore = bookedStartTime.minusHours(1);
            LocalDateTime oneHourAfter = bookedStartTime.plusHours(1);

            if (!now.isBefore(oneHourBefore) && !now.isAfter(oneHourAfter)) {
                isInCriticalPeriod = true;
                break;
            }
        }

        if (isInCriticalPeriod && (gameReq.getConfirm() == null || !gameReq.getConfirm())) {
            throw new Exception("目前時間接近預約時段，請確認是否仍要開台");
        }

        // ✅ 扣儲值金與點數
        int remainingAmount = store.getDeposit();
        int availableBalance = byUid.getAmount() + byUid.getPoint();

        if (availableBalance >= store.getDeposit()) {
            if (byUid.getAmount() >= store.getDeposit()) {
                byUid.setAmount(byUid.getAmount() - store.getDeposit());
            } else {
                remainingAmount -= byUid.getAmount();
                byUid.setAmount(0);
                byUid.setPoint(byUid.getPoint() - remainingAmount);
            }
        } else {
            throw new GameBookingException("儲值金額和額外獎勳不足以支付總金額");
        }

        byUid.setBalance(byUid.getAmount() + byUid.getPoint());
        userRepository.save(byUid);

        // ✅ 處理預約時間衝突與時間提示訊息
        String message = "";
        long endTimeMinutes = 0;
        LocalDateTime startTime = now;

        for (String bookedGame : bookedGames) {
            GameOrder order = gameOrderRepository.findByGameId(bookedGame);
            if (order == null) continue;

            LocalDateTime bookedStartTime = order.getStartTime();
            LocalDateTime bookedEndTime = order.getEndTime();

            if (startTime.isBefore(bookedEndTime) && startTime.plusHours(1).isAfter(bookedStartTime)) {
                long availableTimeMinutes = Duration.between(startTime, bookedStartTime).toMinutes();
                endTimeMinutes = availableTimeMinutes + 5;
                message = "您的遊戲時間 " + bookedEndTime.minusMinutes(5).toLocalTime() + "，之後將會結束並計算費用。";
                startTime = bookedEndTime.minusMinutes(5);
                break;
            }
        }

        // ✅ 建立 GameRecord 並儲存
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString());
        gameRecord.setStartTime(startTime);
        gameRecord.setUserUid(byUid.getUid());
        gameRecord.setPrice(store.getDeposit());
        gameRecord.setStatus("STARTED");
        gameRecord.setStoreId(store.getId());
        gameRecord.setStoreName(store.getName());
        gameRecord.setVendorId(vendor.getId());
        gameRecord.setVendorName(vendor.getName());
        gameRecord.setContactInfo(vendor.getContactInfo());
        gameRecord.setPoolTableId(byStoreUid.getId());
        gameRecord.setPoolTableName(byStoreUid.getTableNumber());
        gameRecord.setHint(store.getHint());
        gameRecord.setRegularRateAmount(regularRate);
        gameRecord.setDiscountRateAmount(discountRate);

        gameRecordRepository.save(gameRecord);

        // ✅ 更新桌台與設備狀態
        byStoreUid.setIsUse(true);
        poolTableRepository.save(byStoreUid);

        List<TableEquipment> byPoolTableId = tableEquipmentRepository.findByPoolTableId(byStoreUid.getId());
        for (TableEquipment table : byPoolTableId) {
            table.setStatus(true);
            tableEquipmentRepository.save(table);
        }

        // **新增：获取当天所有时段信息**
        List<TimeSlotInfo> allTimeSlots = getAllTimeSlotsForDate(store, today);

        // ✅ 回傳資料（包含时段信息）
        GameRes gameRes = new GameRes(gameRecord, message, endTimeMinutes, vendor, store.getContactPhone());
        gameRes.setTimeSlots(allTimeSlots);

        return gameRes;
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
        timeSlots.sort(Comparator.comparing(TimeSlotInfo::getStartTime));

        return timeSlots;
    }

    /**
     * 获取当前时段信息的方法（复用之前的逻辑）
     * @param store 店铺信息
     * @param targetDate 目标日期
     * @return 当前时段信息
     */
    private TimeSlotInfo getCurrentTimeSlotInfo(Store store, LocalDate targetDate) {
        LocalTime currentTime = LocalTime.now();

        // 1. 首先检查是否为特殊日期
        Optional<SpecialDate> specialDateOpt = getTodaySpecialDate(store, targetDate);

        if (specialDateOpt.isPresent()) {
            SpecialDate specialDate = specialDateOpt.get();

            // 在特殊日期的时段中查找当前时间
            if (specialDate.getTimeSlots() != null) {
                for (SpecialTimeSlot slot : specialDate.getTimeSlots()) {
                    if (isTimeInSlot(currentTime, slot.getStartTime(), slot.getEndTime())) {
                        return TimeSlotInfo.builder()
                                .isDiscount(slot.getIsDiscount())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .rate(slot.getIsDiscount() ? slot.getPrice() : specialDate.getRegularRate())
                                .isSpecialDate(true)
                                .timeSlotType(slot.getIsDiscount() ? "DISCOUNT" : "REGULAR")
                                .build();
                    }
                }
            }

            // 如果不在任何时段内，返回特殊日期的默认信息
            return TimeSlotInfo.builder()
                    .isDiscount(false)
                    .startTime(specialDate.getOpenTime())
                    .endTime(specialDate.getCloseTime())
                    .rate(specialDate.getRegularRate())
                    .isSpecialDate(true)
                    .timeSlotType("REGULAR")
                    .build();
        }

        // 2. 不是特殊日期，查找正常营业时段
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

            // 在时段中查找当前时间
            if (slots != null) {
                for (TimeSlot slot : slots) {
                    if (isTimeInSlot(currentTime, slot.getStartTime(), slot.getEndTime())) {
                        return TimeSlotInfo.builder()
                                .isDiscount(slot.getIsDiscount())
                                .startTime(slot.getStartTime())
                                .endTime(slot.getEndTime())
                                .rate(slot.getIsDiscount() ? schedule.getDiscountRate() : schedule.getRegularRate())
                                .isSpecialDate(false)
                                .timeSlotType(slot.getIsDiscount() ? "DISCOUNT" : "REGULAR")
                                .build();
                    }
                }
            }

            // 如果不在任何时段内，返回默认的一般时段
            return TimeSlotInfo.builder()
                    .isDiscount(false)
                    .startTime(schedule.getOpenTime())
                    .endTime(schedule.getCloseTime())
                    .rate(schedule.getRegularRate())
                    .isSpecialDate(false)
                    .timeSlotType("REGULAR")
                    .build();
        }

        // 3. 如果找不到任何排程，返回默认值
        return TimeSlotInfo.builder()
                .isDiscount(false)
                .startTime(LocalTime.of(0, 0))
                .endTime(LocalTime.of(23, 59))
                .rate(0.0)
                .isSpecialDate(false)
                .timeSlotType("REGULAR")
                .build();
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

    // 添加檢查特殊日期的方法
    private boolean isTimeInRange(LocalTime currentTime, LocalTime startTime, LocalTime endTime) {
        // 判断当前时间是否在开始时间和结束时间之间
        return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
    }

    public boolean gameIsUse(String uid){
        // 检查是否有进行中的游戏
        List<GameRecord> ongoingGames = gameRecordRepository.findByUserUidAndStatus(uid, "STARTED");
        if (!ongoingGames.isEmpty()) {
            return true;
        }

        // 检查今天是否已经开过台
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        List<GameRecord> todayGames = gameRecordRepository
                .findByUserUidAndStatusAndStartTimeBetween(uid, "STARTED", startOfDay, endOfDay);

        return !todayGames.isEmpty();
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
        user.setBalance(user.getAmount() + user.getPoint());
        userRepository.save(user);

        // 計算價格
        int adjustedPrice = (int)calculateAdjustedPrice(store.getId(), gameRecord.getStartTime(), endDateTime).getTotalPrice();
        System.out.println("計算出的價格: " + adjustedPrice);

        // 取得桌台資訊
        PoolTable poolTable = poolTableRepository.findById(gameRecord.getPoolTableId())
                .orElseThrow(() -> new Exception("桌台信息未找到"));
        String newGameId = UUID.randomUUID().toString();

        // 建立遊戲訂單
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(user.getUid());
        if (bookGame != null && "COMPLETE".equals(bookGame.getStatus())) {
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
        if(bookGame != null){
            int i = (int) calculateAdjustedPrice(store.getId(), gameRecord.getStartTime(), LocalDateTime.now()).getTotalPrice();
            response.setTotalPrice(i);
        }else{
            response.setTotalPrice(adjustedPrice);
        }
        response.setGameId(newGameId);

        if (bookGame != null && "COMPLETE".equals(bookGame.getStatus())) {
            response.setGameId(newGameId);
        } else {
            response.setGameId(gameReq.getGameId());
        }
        return response;
    }

    private GamePriceRes calculateAdjustedPrice(Long storeId, LocalDateTime startTime, LocalDateTime endTime) throws Exception {
        double totalPrice = 0;
        long totalDiscountMinutes = 0;
        long totalRegularMinutes = 0;
        long totalEffectiveMinutes = 0;
        long totalNonBusinessHoursMinutes = 0;
        double discountPrice = 0.0;
        double regularPrice = 0.0;
        GamePriceRes gamePriceRes = new GamePriceRes();

        // 原始總分鐘，使用調整後的計時規則
        long totalRawMinutes = adjustMinutes(Duration.between(startTime, endTime));

        LocalDateTime currentStart = startTime;
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new Exception("找不到店家"));

        System.out.println("=== 開始價格計算 ===");
        System.out.println("計算時間範圍: " + startTime + " ~ " + endTime);
        System.out.println("總原始分鐘數: " + totalRawMinutes);

        while (currentStart.isBefore(endTime)) {
            // 處理每一天的價格
            LocalDate currentDate = currentStart.toLocalDate();
            LocalDateTime nextDay = currentDate.plusDays(1).atStartOfDay();
            LocalDateTime currentEnd = endTime.isBefore(nextDay) ? endTime : nextDay;

            System.out.println("\n--- 處理日期: " + currentDate + " ---");
            System.out.println("當日時間範圍: " + currentStart + " ~ " + currentEnd);

            // 檢查當天是否為特殊日期
            Optional<SpecialDate> specialDate = getTodaySpecialDate(store, currentDate);

            if (specialDate.isPresent()) {
                System.out.println("使用特殊日期價格");
                // 特殊日期處理邏輯保持不變
                SpecialDate spDate = specialDate.get();
                // ... 特殊日期的處理邏輯 ...
            } else {
                System.out.println("使用一般日期價格");
                // 使用一般日期的價格和時段
                String dayOfWeek = currentDate.getDayOfWeek().toString().toLowerCase();
                StorePricingSchedule schedule = findScheduleForDay(storeId, dayOfWeek);

                System.out.println("星期: " + dayOfWeek);
                System.out.println("一般費率: " + schedule.getRegularRate());
                System.out.println("優惠費率: " + schedule.getDiscountRate());

                LocalTime openTime = schedule.getOpenTime();
                LocalTime closeTime = schedule.getCloseTime();

                System.out.println("營業時間: " + openTime + " ~ " + closeTime);

                // 檢查是否24小時營業
                boolean is24HourOperation = false;
                if (openTime.equals(LocalTime.of(0, 0)) &&
                        (closeTime.equals(LocalTime.of(23, 59)) || closeTime.equals(LocalTime.of(23, 59, 59)))) {
                    is24HourOperation = true;
                }
                if (openTime.equals(closeTime)) {
                    is24HourOperation = true;
                }

                System.out.println("是否24小時營業: " + is24HourOperation);

                // 計算當日總時間
                long totalDayMinutes = adjustMinutes(Duration.between(currentStart, currentEnd));
                System.out.println("當日總分鐘數: " + totalDayMinutes);

                // 計算營業時間內的時間
                LocalDateTime businessStart = currentStart;
                LocalDateTime businessEnd = currentEnd;

                // 非24小時營業時，計算營業時間與非營業時間
                if (!is24HourOperation) {
                    // 創建營業開始和結束的日期時間
                    LocalDateTime dayOpenTime = currentDate.atTime(openTime);
                    LocalDateTime dayCloseTime = currentDate.atTime(closeTime);

                    // 處理跨日的情況
                    if (closeTime.isBefore(openTime)) {
                        dayCloseTime = dayCloseTime.plusDays(1);
                    }

                    System.out.println("營業時間段: " + dayOpenTime + " ~ " + dayCloseTime);

                    // 計算營業時間內的時段
                    if (currentEnd.isBefore(dayOpenTime) || currentStart.isAfter(dayCloseTime)) {
                        System.out.println("完全不在營業時間內");
                        businessStart = null;
                        businessEnd = null;
                    } else {
                        if (currentStart.isBefore(dayOpenTime)) {
                            businessStart = dayOpenTime;
                        }
                        if (currentEnd.isAfter(dayCloseTime)) {
                            businessEnd = dayCloseTime;
                        }
                        System.out.println("調整後營業時間段: " + businessStart + " ~ " + businessEnd);
                    }
                }

                // 處理營業時間內的費用計算
                long businessHoursMinutes = 0;
                if (businessStart != null && businessEnd != null && businessStart.isBefore(businessEnd)) {
                    businessHoursMinutes = adjustMinutes(Duration.between(businessStart, businessEnd));
                    System.out.println("營業時間內分鐘數: " + businessHoursMinutes);

                    // **修正：計算所有優惠時段，而不是只取第一個**
                    List<TimeSlot> discountSlots = schedule.getTimeSlots().stream()
                            .filter(TimeSlot::getIsDiscount)
                            .collect(Collectors.toList());

                    System.out.println("找到 " + discountSlots.size() + " 個優惠時段");

                    long totalDiscountMinutesForDay = 0;

                    // 遍歷所有優惠時段
                    for (TimeSlot discountSlot : discountSlots) {
                        LocalTime discountStart = discountSlot.getStartTime();
                        LocalTime discountEnd = discountSlot.getEndTime();

                        System.out.println("處理優惠時段: " + discountStart + " ~ " + discountEnd);

                        LocalDateTime discSlotStart = businessStart.toLocalDate().atTime(discountStart);
                        LocalDateTime discSlotEnd = businessStart.toLocalDate().atTime(discountEnd);

                        // 處理跨日優惠時段
                        if (discountEnd.isBefore(discountStart)) {
                            discSlotEnd = discSlotEnd.plusDays(1);
                            System.out.println("跨日優惠時段，調整為: " + discSlotStart + " ~ " + discSlotEnd);
                        }

                        // 計算優惠時段與營業時間的重疊部分
                        if (discSlotStart.isBefore(businessEnd) && discSlotEnd.isAfter(businessStart)) {
                            LocalDateTime overlapStart = discSlotStart.isAfter(businessStart) ? discSlotStart : businessStart;
                            LocalDateTime overlapEnd = discSlotEnd.isBefore(businessEnd) ? discSlotEnd : businessEnd;

                            long discountMinutes = adjustMinutes(Duration.between(overlapStart, overlapEnd));
                            System.out.println("優惠時段重疊: " + overlapStart + " ~ " + overlapEnd + " (" + discountMinutes + "分鐘)");

                            totalDiscountMinutesForDay += discountMinutes;
                        } else {
                            System.out.println("優惠時段無重疊");
                        }
                    }

                    // **修正：避免重複計算優惠時段**
                    // 如果有多個優惠時段重疊，需要去重
                    if (totalDiscountMinutesForDay > businessHoursMinutes) {
                        System.out.println("警告：優惠時段總和超過營業時間，調整為營業時間");
                        totalDiscountMinutesForDay = businessHoursMinutes;
                    }

                    long regularMinutesForDay = businessHoursMinutes - totalDiscountMinutesForDay;

                    System.out.println("當日優惠分鐘數: " + totalDiscountMinutesForDay);
                    System.out.println("當日一般分鐘數: " + regularMinutesForDay);

                    double discountRate = schedule.getDiscountRate();
                    double regularRate = schedule.getRegularRate();

                    // 計算並累加優惠時段的價格和一般時段的價格
                    double currentDiscountPrice = totalDiscountMinutesForDay * discountRate;
                    double currentRegularPrice = regularMinutesForDay * regularRate;

                    System.out.println("當日優惠價格: " + currentDiscountPrice);
                    System.out.println("當日一般價格: " + currentRegularPrice);

                    discountPrice += currentDiscountPrice;
                    regularPrice += currentRegularPrice;
                    totalPrice += currentDiscountPrice + currentRegularPrice;

                    totalDiscountMinutes += totalDiscountMinutesForDay;
                    totalRegularMinutes += regularMinutesForDay;
                    totalEffectiveMinutes += businessHoursMinutes;
                }

                // 計算非營業時間的費用
                long nonBusinessHoursMinutes = totalDayMinutes - businessHoursMinutes;
                if (nonBusinessHoursMinutes > 0) {
                    double regularRate = schedule.getRegularRate();
                    double nonBusinessPrice = nonBusinessHoursMinutes * regularRate;

                    System.out.println("非營業時間分鐘數: " + nonBusinessHoursMinutes);
                    System.out.println("非營業時間價格: " + nonBusinessPrice);

                    totalPrice += nonBusinessPrice;
                    regularPrice += nonBusinessPrice;
                    totalRegularMinutes += nonBusinessHoursMinutes;
                    totalNonBusinessHoursMinutes += nonBusinessHoursMinutes;
                    totalEffectiveMinutes += nonBusinessHoursMinutes;
                }
            }

            currentStart = currentEnd;
        }

        System.out.println("\n=== 計算結果 ===");
        System.out.println("總共遊玩時間(原始): " + totalRawMinutes + " 分鐘");
        System.out.println("總優惠時段: " + totalDiscountMinutes + " 分鐘");
        System.out.println("總一般時段: " + totalRegularMinutes + " 分鐘");
        System.out.println("總非營業時間: " + totalNonBusinessHoursMinutes + " 分鐘");
        System.out.println("總金額: " + totalPrice);
        System.out.println("優惠金額: " + discountPrice);
        System.out.println("一般金額: " + regularPrice);

        gamePriceRes.setDiscountPrice(discountPrice);
        gamePriceRes.setRegularPrice(regularPrice);
        gamePriceRes.setDeposit(store.getDeposit());
        gamePriceRes.setTotalRawMinutes(totalRawMinutes);
        gamePriceRes.setTotalDiscountMinutes(totalDiscountMinutes);
        gamePriceRes.setTotalRegularMinutes(totalRegularMinutes);
        gamePriceRes.setTotalPrice(totalPrice);
        return gamePriceRes;
    }

    // 添加檢查特殊日期的方法，與 StoreService 中的類似
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
        User byUid = userRepository.findByUid(byGameId.getUserUid());
        GameReq gameReq = new GameReq();
        gameReq.setGameId(checkoutReq.getGameId());
        gameReq.setPoolTableUId(byId.get().getUid());
        GameResponse gameResponse = this.endGame(gameReq, byUid.getId());
        Integer totalPrice = (int)gameResponse.getTotalPrice();

        String gameId = gameResponse.getGameId();
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
                    throw new GameBookingException("儲值金額和額外獎勳不足以支付總金額");
                }
                availableBalance = user.getAmount() + user.getPoint();
                user.setBalance((int) availableBalance);
                // 儲值金扣除後保存更新后的用戶數據
                userRepository.save(user);
                break;

            case "2": // Apple Pay

                break;

            case "3": // Google Pay

                break;
            case "4": // Google Pay

                break;
            default:
                throw new GameBookingException("无效的支付方式");
        }
        GameOrder game = gameOrderRepository.findByGameId(gameId);
        PoolTable poolTable = poolTableRepository.findById(checkoutReq.getPoolTableId()).get();
        Store store = storeRepository.findById(poolTable.getStore().getId()).get();
        Long id1 = store.getVendor().getId();
        Vendor vendor = vendorRepository.findById(id1).get();
        // 创建交易记录
        GameTransactionRecord transactionRecord = GameTransactionRecord.builder()
                .uid(user.getUid())
                .amount(totalPrice)
                .vendorName(vendor.getName())
                .storeName(store.getName()) // 假设有商店名
                .tableNumber(poolTable.getTableNumber()) // 假设有桌号
                .transactionDate(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .transactionType("CONSUME")
                .user(user)
                .vendorId(vendor.getId())
                .storeId(store.getId())
                .tableId(poolTable.getId())
                .build();

        // 保存交易记录
        gameTransactionRecordRepository.save(transactionRecord);

        game.setStatus("IS_PAY");
        gameOrderRepository.save(game);

        return new GameRes(null , null , 0L , vendor , store.getContactPhone());
    }

    public GameRecord bookGame(BookGameReq gameReq) throws Exception {
        User byUid = userRepository.findById(SecurityUtils.getSecurityUser().getId()).orElseThrow(
                () -> new Exception("使用者不存在"));

        boolean c = this.checkPooltable(gameReq.getPoolTableUId());
        if(c){
            throw new Exception("球局目前不開放使用，請換別桌進行球局");
        }

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
        Double discountRateAmount = currentSchedule.getDiscountRate();
        Double regularRateAmount = currentSchedule.getRegularRate();
        long durationHours = Duration.between(gameReq.getStartTime(), gameReq.getEndTime()).toHours();
        if (durationHours <= 0) {
            throw new Exception("預約時間必須至少為1小時");
        }
        int bookDeposit = (int) (store.getDeposit() * durationHours);
        int recordDeposit = bookDeposit;


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
        int availableBalance = byUid.getAmount() + byUid.getPoint();
        if (availableBalance >= bookDeposit) {
            // 儲值金額足夠
            if (byUid.getAmount() >= bookDeposit) {
                byUid.setAmount((int) (byUid.getAmount() - bookDeposit));
                bookDeposit = 0;
            } else {
                // 儲值金額不足，扣光它，剩下的再從額外金額扣
                bookDeposit -= byUid.getAmount();
                byUid.setAmount(0);

                byUid.setPoint((int) (byUid.getPoint() - bookDeposit));
                bookDeposit = 0;
            }
        } else {
            // 餘額不足
            throw new GameBookingException("儲值金額和額外獎勳不足以支付總金額");
        }
        availableBalance = byUid.getAmount() + byUid.getPoint();
        byUid.setBalance((int) availableBalance);
        // 儲值金扣除後保存更新后的用戶數據
        userRepository.save(byUid);
        LocalDateTime endTime = gameReq.getEndTime();
        // ➡️ 建立 GameRecord
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString());
        gameRecord.setUserUid(byUid.getUid());
        gameRecord.setStartTime(startOfDay);
        gameRecord.setPrice(recordDeposit);
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
        gameOrder.setTotalPrice(recordDeposit);
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
        GameOrder byGameId1 = gameOrderRepository.findByGameId(gameReq.getGameId());
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
        int bookDeposit = 0;
        if (minutesUntilStart > cancelBookTime) {
            // 超過設定時間，退還全部訂金
            long durationHours = Duration.between(byGameId1.getStartTime(), byGameId1.getEndTime()).toHours();
            if (durationHours <= 0) {
                throw new Exception("預約時間必須至少為1小時");
            }
            bookDeposit = gameRecord.getPrice();
            byUid.setPoint(byUid.getPoint() + bookDeposit);
            byUid.setBalance(byUid.getAmount() + byUid.getPoint());
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
        gameOrder.setTotalPrice(bookDeposit); // 退款金額
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
    public Map<String, List<Map<String, Object>>> getAvailableTimes(Long storeId, LocalDate bookingDate, Long poolTableId) throws Exception {
        int duration = 1;
        int maxSlots = 24;

        boolean isToday = bookingDate.equals(LocalDate.now());
        LocalTime now = LocalTime.now();

        // 檢查是否是特殊日期
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new Exception("未找到指定商店"));

        // 檢查是否為特殊日期
        Optional<SpecialDate> specialDateOpt = store.getSpecialDates().stream()
                .filter(sd -> {
                    if (sd.getDate() instanceof LocalDate) {
                        return ((LocalDate) sd.getDate()).equals(bookingDate);
                    }
                    return false;
                })
                .findFirst();

        LocalTime openTime;
        LocalTime closeTime;
        Double regularRate;

        if (specialDateOpt.isPresent()) {
            // 如果是特殊日期，使用特殊日期的營業時間和費率
            SpecialDate specialDate = specialDateOpt.get();
            openTime = specialDate.getOpenTime();
            closeTime = specialDate.getCloseTime();
            regularRate = specialDate.getRegularRate();
        } else {
            // 如果不是特殊日期，使用正常排程
            StorePricingSchedule schedule = storePricingScheduleRepository.findByStoreId(storeId)
                    .stream()
                    .filter(s -> s.getDayOfWeek().equalsIgnoreCase(bookingDate.getDayOfWeek().toString()))
                    .findFirst()
                    .orElseThrow(() -> new Exception("未找到對應日期的時段"));

            openTime = schedule.getOpenTime();
            closeTime = schedule.getCloseTime();
            regularRate = schedule.getRegularRate();
        }

        PoolTable poolTable = poolTableRepository.findById(poolTableId)
                .orElseThrow(() -> new Exception("未找到指定桌台"));

        List<Object[]> results = gameRecordRepository.findGameIdsByStoreIdStatusAndPoolTableId(storeId, poolTableId);

        List<GameVO> gameVOList = results.stream()
                .map(result -> new GameVO((String) result[0], (String) result[1]))
                .collect(Collectors.toList());

        List<GameVO> bookedGames = gameVOList.stream()
                .filter(x -> "BOOK".equals(x.getStatus()))
                .collect(Collectors.toList());

        Map<String, List<Map<String, Object>>> availableTimesMap = new HashMap<>();
        List<Map<String, Object>> availableTimes = new ArrayList<>();

        // 生成時段 (嚴格限制在營業時間內)
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

            // 根據特殊日期或一般排程獲取費率
            Double rate;
            if (specialDateOpt.isPresent()) {
                rate = getRateForSpecialDate(specialDateOpt.get(), startTime, regularRate);
            } else {
                // 獲取普通日期的費率
                StorePricingSchedule schedule = storePricingScheduleRepository.findByStoreId(storeId)
                        .stream()
                        .filter(s -> s.getDayOfWeek().equalsIgnoreCase(bookingDate.getDayOfWeek().toString()))
                        .findFirst()
                        .orElseThrow(() -> new Exception("未找到對應日期的時段"));

                rate = getRateForNormalDate(schedule, startTime);
            }

            Map<String, Object> availableTimeSlot = new HashMap<>();
            availableTimeSlot.put("start", startTime.toString());
            availableTimeSlot.put("end", endTime.toString());
            availableTimeSlot.put("rate", rate);
            availableTimes.add(availableTimeSlot);
        }

        availableTimesMap.put(String.valueOf(poolTableId), availableTimes);
        return availableTimesMap;
    }

    // 生成營業時間內的所有小時時段
    private List<LocalTime> generateTimeSlots(LocalTime openTime, LocalTime closeTime) {
        List<LocalTime> timeSlots = new ArrayList<>();

        // 判斷是否為24小時營業或跨午夜營業
        boolean is24HoursOperation = openTime.equals(closeTime);
        boolean crossesMidnight = !is24HoursOperation && openTime.isAfter(closeTime);

        // 特殊情況：closeTime 接近午夜（如 23:59）且需要包含 23:00 時段
        boolean closeAtMidnight = closeTime.getHour() == 23 && closeTime.getMinute() >= 59;

        // 生成時段，但限制在營業時間內
        for (int hour = 0; hour < 24; hour++) {
            LocalTime currentTime = LocalTime.of(hour, 0);

            if (is24HoursOperation) {
                // 24小時營業，添加所有時段
                timeSlots.add(currentTime);
            } else if (crossesMidnight) {
                // 跨午夜營業，例如從晚上10點到早上6點
                if (!currentTime.isBefore(openTime) || !currentTime.isAfter(closeTime)) {
                    timeSlots.add(currentTime);
                }
            } else {
                // 正常營業時間
                if (!currentTime.isBefore(openTime)) {
                    // 特殊處理 23:00 時段，當 closeTime 是 23:59 時
                    if (closeAtMidnight) {
                        if (currentTime.getHour() <= 23) {
                            timeSlots.add(currentTime);
                        }
                    } else {
                        // 一般情況
                        if (currentTime.isBefore(closeTime)) {
                            timeSlots.add(currentTime);
                        }
                    }
                }
            }
        }

        return timeSlots;
    }

    // 獲取特殊日期的費率
    private Double getRateForSpecialDate(SpecialDate specialDate, LocalTime startTime, Double defaultRate) {
        // 查詢特殊日期的 SpecialTimeSlot
        for (SpecialTimeSlot slot : specialDate.getTimeSlots()) {
            if (!startTime.isBefore(slot.getStartTime()) && startTime.isBefore(slot.getEndTime())) {
                return slot.getIsDiscount() ? slot.getPrice() : defaultRate;
            }
        }
        return defaultRate;
    }

    // 獲取普通日期的費率
    private Double getRateForNormalDate(StorePricingSchedule schedule, LocalTime startTime) {
        // 查詢普通日期的 TimeSlot
        for (TimeSlot slot : schedule.getTimeSlots()) {
            if (!startTime.isBefore(slot.getStartTime()) && startTime.isBefore(slot.getEndTime())) {
                return slot.getIsDiscount() ? schedule.getDiscountRate() : schedule.getRegularRate();
            }
        }
        return schedule.getRegularRate();
    }
    // 過濾掉已預約的時段
    private List<LocalTime> filterBookedTimeSlots(List<LocalTime> availableStartTimes,
                                                  List<GameOrder> relevantBookings,
                                                  LocalDate bookingDate,
                                                  int duration) {
        return availableStartTimes.stream()
                .filter(startTime -> {
                    // 處理 23:00-00:00 的特殊情況
                    LocalTime endTime;
                    if (startTime.getHour() == 23) {
                        endTime = LocalTime.of(0, 0);
                    } else {
                        endTime = startTime.plusHours(duration);
                    }

                    LocalDateTime slotStartDateTime = bookingDate.atTime(startTime);
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
    private static Double getRateForTime(List<TimeSlot> timeSlots, StorePricingSchedule schedule, LocalTime startTime) {
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

    public GamePriceRes getGamePrice(GameReq gameReq) throws Exception {
        // 取得遊戲紀錄
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());
        if (byGameId == null) {
            throw new Exception("找不到遊戲紀錄");
        }

        // 取得店家信息
        Store store = storeRepository.findById(byGameId.getStoreId())
                .orElseThrow(() -> new Exception("找不到店家"));

        // 取得開始和結束時間
        LocalDateTime startTime = byGameId.getStartTime();
        LocalDateTime endTime = LocalDateTime.now();

        // 驗證結束時間
        if (endTime.isBefore(startTime)) {
            throw new Exception("結束時間不能小於開始時間");
        }

        // 計算總秒數
        long totalSeconds = ChronoUnit.SECONDS.between(startTime, endTime);

        try {
            // 使用與endGame相同的價格計算邏輯
            GamePriceRes gamePriceRes = calculateAdjustedPrice(byGameId.getStoreId(), startTime, endTime);
            gamePriceRes.setSecond(totalSeconds);

            // **新增：计算并设置新字段**

            // 获取费率信息 (需要从游戏记录或重新计算)
            double regularRate = byGameId.getRegularRateAmount(); // 每分钟费率
            double discountRate = byGameId.getDiscountRateAmount(); // 每分钟费率

            // 设置每小时费率
            gamePriceRes.setRegularHourlyRate(regularRate * 60);
            gamePriceRes.setDiscountHourlyRate(discountRate * 60);

            // 设置球台租金 (负数，表示已支付)
            gamePriceRes.setTableRental(-store.getDeposit().doubleValue());

            // 计算最终金额 = 游戏费用 - 已付押金
            // 如果是负数 = 要退款给客户
            // 如果是正数 = 客户还要付钱
            double finalAmount = gamePriceRes.getTotalPrice() - store.getDeposit();
            gamePriceRes.setFinalAmount(finalAmount);

            // 回傳計算結果
            return gamePriceRes;
        } catch (Exception e) {
            throw new Exception("計算價格時發生錯誤: " + e.getMessage());
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


    private boolean checkPooltable(String poolTableUID){
        PoolTable poolTable = poolTableRepository.findByUid(poolTableUID).get();
        if(!poolTable.getStatus().equals("AVAILABLE")){
            return true;
        }
        return false;
    }

}
