package com.frontend.service;


import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.GameOrderRepository;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.UserRepository;
import com.frontend.req.game.GameReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class GameService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private GameOrderRepository gameOrderRepository;

    public void startGame(GameReq gameReq) throws Exception {
        // 查詢用戶
        User byUid = userRepository.findByUid(gameReq.getUserUid());

        // 扣除押金
        int newAmount = byUid.getAmount() - gameReq.getPrice();
        if (newAmount < 0) {
            throw new Exception("押金不足，請儲值");
        } else {
            byUid.setAmount(newAmount);
            userRepository.save(byUid);
        }

        // 計算遊戲開始時間
        LocalDateTime startTime = LocalDateTime.now();// 獲取當前時間戳

        // 創建遊戲紀錄並保存
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(UUID.randomUUID().toString()); // 生成UUID
        gameRecord.setStartTime(startTime);
        gameRecord.setUserUid(gameReq.getUserUid());
        gameRecord.setPrice(gameReq.getPrice());
        gameRecord.setStatus("STARTED"); // 設置狀態為開始
        gameRecordRepository.save(gameRecord);  // 儲存遊戲紀錄

        //開啟桌台使用

        // 返回時間戳或其他需要的資料給前端
        // 例如：gameRecord.getStartTime() 或者使用時間戳轉換


    }

    public void endGame(GameReq gameReq) throws Exception {

        //計算遊玩時間
        // Step 1: 取得遊戲的開始和結束時間
        GameRecord byGameId = gameRecordRepository.findByGameId(gameReq.getGameId());
        LocalDateTime startTime = byGameId.getStartTime();
        LocalDateTime endTime = gameReq.getEndTime();

        // 確保結束時間不早於開始時間
        if (endTime.isBefore(startTime)) {
            throw new Exception("結束時間不能早於開始時間");
        }

        // Step 2: 計算兩者之間的時間差
        Duration duration = Duration.between(startTime, endTime);  // 計算時間差

        // Step 3: 取得總分鐘數
        long totalMinutes = duration.toMinutes();

        // Step 4: 計算以小時計算的總時長
        long totalHours = (long) Math.ceil((double) totalMinutes / 60);  // 向上取整

        // Step 5: 根據總時長計算價格（假設每小時價格為 pricePerHour）
        int pricePerHour = gameReq.getGamePrice();  // 假設每小時的價格為 100
        double totalPrice = totalHours * pricePerHour;

        //扣除遊玩時間算法
        User byUid = userRepository.findByUid(gameReq.getUserUid());
        int newAmount = byUid.getAmount() - (int)totalPrice;
        if (newAmount < 0) {
            throw new Exception("押金不足，請儲值");
        } else {
            byUid.setAmount(newAmount);
            userRepository.save(byUid);
        }

        // Step 7: 創建遊玩訂單（假設 GameOrder 是遊戲訂單實體）
        GameOrder gameOrder = new GameOrder();
        gameOrder.setUserId(gameReq.getUserUid());
        gameOrder.setGameId(gameReq.getGameId());
        gameOrder.setTotalPrice(totalPrice);
        gameOrder.setStartTime(startTime);
        gameOrder.setEndTime(endTime);
        gameOrder.setDuration(totalHours);

        gameOrderRepository.save(gameOrder);  // 儲存遊戲訂單


        //結束押金table的狀態為end

        //關閉桌台使用

    }
}
