package com.frontend.service;

import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.repo.*;
import com.frontend.res.game.GameOrderRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
                        null
                ))
                .toList();
    }

}
