package com.frontend.controller.admin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import com.frontend.entity.game.BookGame;
import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.user.User;
import com.frontend.repo.*;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameResponse;
import com.frontend.res.poolTable.AdminPoolTableRes;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.utils.RandomUtils;

@Service
public class AdminPoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;
    @Autowired
    private TableEquipmentRepository tableEquipmentRepository;
    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameOrderRepository gameOrderRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private BookGameRepository bookGameRepository;

    @Autowired
    private StorePricingScheduleRepository storePricingScheduleRepository;

    // Create a new pool table
    public PoolTable createPoolTable(PoolTableReq poolTableReq, Long id) {
        PoolTable poolTable = convertToEntity(poolTableReq);
        poolTable.setUid(RandomUtils.genRandom(24)); // 生成唯一 UID
        poolTable.setCreateTime(LocalDateTime.now());
        poolTable.setCreateUserId(id);
        return poolTableRepository.save(poolTable);
    }

    private PoolTable convertToEntity(PoolTableReq req) {
        PoolTable poolTable = new PoolTable();
        poolTable.setTableNumber(req.getTableNumber());
        poolTable.setStatus(req.getStatus());
        if(req.getStore() != null){
            poolTable.setStore(req.getStore());
        }
        // 這裡假設 Store 是直接從 PoolTableReq 傳過來的
//        if(req.getTableEquipments() != null){
//            poolTable.setTableEquipments(req.getTableEquipments());
//        }
        poolTable.setIsUse(false);

        return poolTable;
    }


    public Optional<AdminPoolTableRes> getPoolTableById(String uid) {
        Optional<PoolTable> poolTable = poolTableRepository.findByUid(uid);
        if (poolTable.isPresent()) {
            AdminPoolTableRes adminPoolTableRes = convertToAdminPoolTableRes(poolTable.get());
            return Optional.of(adminPoolTableRes);
        }
        return Optional.empty();
    }

    public List<AdminPoolTableRes> getAllPoolTables() {
        List<PoolTable> poolTables = poolTableRepository.findAll();
        return poolTables.stream()
                .map(this::convertToAdminPoolTableRes)
                .collect(Collectors.toList());
    }

    private AdminPoolTableRes convertToAdminPoolTableRes(PoolTable poolTable) {
        AdminPoolTableRes.AdminPoolTableResBuilder builder = AdminPoolTableRes.builder()
                .storeId(poolTable.getStore().getId())
                .uid(poolTable.getUid())
                .tableNumber(poolTable.getTableNumber())
                .status(poolTable.getStatus());

        // 只在 tableEquipments 不为 null 时设置 tableEquipments
//        if (poolTable.getTableEquipments() != null) {
//            builder.tableEquipments(poolTable.getTableEquipments());
//        }

        return builder.build();
    }



    // Update a pool table
    public PoolTable updatePoolTable(String uid, PoolTableReq updatedPoolTableReq, Long id) {
        if(updatedPoolTableReq.getStatus().equals("FAULT")){
            PoolTable poolTable = poolTableRepository.findByUid(uid).get();
            List<GameRecord> book = gameRecordRepository.findAllByPoolTableIdAndStatus(poolTable.getId(), "BOOK");
            for (GameRecord gameRecord : book) {
                gameRecord.setStatus("CANCEL");
                gameRecordRepository.save(gameRecord);

                User byUid = userRepository.findByUid(gameRecord.getUserUid());
                byUid.setBalance(gameRecord.getPrice());
                userRepository.save(byUid);
            }
            poolTable.setStatus("FAULT");
            return poolTableRepository.save(poolTable);
        }else{
            // 根據 UID 查找原有的 PoolTable
            return poolTableRepository.findByUid(uid).map(poolTable -> {
                // 使用更新的 PoolTableReq 來設置新的數據
                poolTable.setTableNumber(updatedPoolTableReq.getTableNumber());
                poolTable.setStatus(updatedPoolTableReq.getStatus());
                poolTable.setIsUse(updatedPoolTableReq.getIsUse());
                if(updatedPoolTableReq.getStore() != null){
                    poolTable.setStore(updatedPoolTableReq.getStore());
                }
                // 這裡假設 Store 是直接從 PoolTableReq 傳過來的
//            if(updatedPoolTableReq.getTableEquipments() != null){
//                poolTable.setTableEquipments(updatedPoolTableReq.getTableEquipments());
//            }
                // 設置時間和用戶信息
                poolTable.setUpdateTime(LocalDateTime.now());
                poolTable.setUpdateUserId(id);

                // 保存更新後的 PoolTable
                return poolTableRepository.save(poolTable);
            }).orElseThrow(() -> new RuntimeException("PoolTable not found with uid: " + uid));
        }
    }


    // Delete a pool table
    @Transactional
    public void deletePoolTable(String uid) {
        poolTableRepository.deleteByUid(uid);
    }
    
    public List<PoolTable> findByStoreId(Long storeId) {
        List<PoolTable> poolTables = poolTableRepository.findByStoreId(storeId);

        return poolTables;
    }

    public PoolTable closePoolTable(PoolTableReq poolTableReq, Long id) throws Exception {
        PoolTable poolTable = poolTableRepository.findByUid(poolTableReq.getTableUId()).get();
        poolTable.setIsUse(false);
        List<TableEquipment> byPoolTableId = tableEquipmentRepository.findByPoolTableId(poolTable.getId());
        for (TableEquipment tableEquipment : byPoolTableId) {
            tableEquipment.setStatus(false);
            tableEquipmentRepository.save(tableEquipment);
        }
        poolTable.setUpdateTime(LocalDateTime.now());
        poolTable.setUpdateUserId(id);
        poolTableRepository.save(poolTable);

        GameRecord started = gameRecordRepository.findByPoolTableIdAndStatus(poolTable.getId(), "STARTED");
        if(started != null){
            GameReq gameReq = new GameReq();
            gameReq.setGameId(started.getGameId());
            gameReq.setPoolTableUId(poolTableReq.getTableUId());
            gameReq.setPoolTableId(poolTable.getId());
            this.endGame(gameReq , id);
        }
        return poolTable;
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

        // 建立遊戲訂單
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(user.getUid());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setTotalPrice(adjustedPrice);
        gameOrder.setStartTime(gameRecord.getStartTime());
        gameOrder.setEndTime(endDateTime);
        gameOrder.setDuration(totalMinutes);
        gameOrder.setStatus("NO_PAY");
        gameOrder.setPoolTableUid(poolTable.getUid());
        gameOrderRepository.save(gameOrder);

        // 更新桌台和遊戲紀錄
        poolTable.setIsUse(false);
        poolTableRepository.save(poolTable);

        gameRecord.setPrice(adjustedPrice);
        gameRecord.setStatus("UNPAID");
        gameRecordRepository.save(gameRecord);

        // 建立回應
        GameResponse response = new GameResponse();
        response.setTotalSeconds(duration.toSeconds());
        response.setTotalPrice(adjustedPrice);
        response.setGameId(gameReq.getGameId());

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

    private StorePricingSchedule findScheduleForDay(Long storeId, String dayOfWeek) throws Exception {
        // 根據店鋪 ID 查找對應的時段設置
        List<StorePricingSchedule> schedules = storePricingScheduleRepository.findByStoreId(storeId);
        return schedules.stream()
                .filter(schedule -> schedule.getDayOfWeek().equalsIgnoreCase(dayOfWeek))
                .findFirst()
                .orElseThrow(() -> new Exception("找不到 " + dayOfWeek + " 的時段設定"));
    }
}
