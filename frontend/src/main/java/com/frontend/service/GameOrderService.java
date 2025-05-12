package com.frontend.service;

import com.frontend.config.GameBookingException;
import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameOrderRes;
import com.frontend.res.game.GameRes;
import com.frontend.res.game.GameResponse;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GameOrderService {

    @Autowired
    private GameOrderRepository gameOrderRepository;

    @Autowired
    private UserRepository  userRepository;

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private GameTransactionRecordRepository gameTransactionRecordRepository;

    public List<GameOrderRes> getOrderByUserId(Long id) {
        User user = userRepository.findById(id).get();
        List<GameOrder> byUserUid = gameOrderRepository.findByUserId(user.getUid());
        List<GameOrderRes> gameOrderResList = toGameOrderResList(byUserUid);
        if(!gameOrderResList.isEmpty()){
            for (GameOrderRes orderRes : gameOrderResList) {
                if(orderRes.getGameId() != null){
                    GameRecord byGameId = gameRecordRepository.findByGameId(orderRes.getGameId());
                    if(byGameId != null){
                        Store store = storeRepository.findById(byGameId.getStoreId()).get();
                        PoolTable poolTable = poolTableRepository.findById(byGameId.getPoolTableId()).get();
                        String format = String.format("%s - %s", store.getName(), poolTable.getTableNumber());
                        orderRes.setGameOrderName(format);
                    }
                }

            }
        }

        return gameOrderResList;
    }

    public List<GameOrderRes> toGameOrderResList(List<GameOrder> gameOrders) {
        return gameOrders.stream()
                .map(gameOrder -> new GameOrderRes(
                        gameOrder.getGameId(),
                        gameOrder.getTotalPrice(),
                        gameOrder.getStartTime(),
                        gameOrder.getEndTime(),
                        gameOrder.getDuration(),
                        gameOrder.getStatus(),
                        null,
                        gameOrder.getPoolTableUid()
                ))
                .toList();
    }

    public GameRes checkout(CheckoutReq checkoutReq, Long id) {
        if(checkoutReq.getGameId() == null){
            PoolTable poolTable = poolTableRepository.findById(checkoutReq.getPoolTableId()).get();
            GameRecord gameRecord = gameRecordRepository.findByPoolTableIdAndStatus(poolTable.getId() , "STARTED");
            checkoutReq.setGameId(gameRecord.getGameId());
        }

        // 获取当前用户
        User user = userRepository.findById(id).get(); // 假设有一个获取当前用户的方式
        // 根据支付类型进行判断
        switch (checkoutReq.getPayType()) {
            case "1": // 儲值金支付
                int remainingAmount = checkoutReq.getTotalPrice();

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
                // 在这里处理Apple Pay支付（可以调用第三方支付接口）
                // 这里只是示意，实际支付处理需要集成相关支付SDK
                break;

            case "3": // Google Pay
                // 在这里处理Google Pay支付（可以调用第三方支付接口）
                // 这里只是示意，实际支付处理需要集成相关支付SDK
                break;
            case "4":

            default:
                throw new RuntimeException("无效的支付方式");
        }
        GameOrder game = gameOrderRepository.findByGameId(checkoutReq.getGameId());
        GameRecord byGameId = gameRecordRepository.findByGameId(checkoutReq.getGameId());

        byGameId.setStatus("ENDED");
        gameRecordRepository.save(byGameId);


        PoolTable poolTable = poolTableRepository.findById(byGameId.getPoolTableId()).get();
        Store store = storeRepository.findById(poolTable.getStore().getId()).get();
        Long id1 = store.getVendor().getId();
        Vendor vendor = vendorRepository.findById(id1).get();
        // 创建交易记录
        GameTransactionRecord transactionRecord = GameTransactionRecord.builder()
                .uid(user.getUid())
                .amount(checkoutReq.getTotalPrice())
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

        return new GameRes(null , null , 0L , vendor , store.getContactPhone());
    }
}
