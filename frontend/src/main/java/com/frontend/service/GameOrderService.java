package com.frontend.service;

import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.GameOrderRepository;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.UserRepository;
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

    public List<GameOrderRes> getOrderByUserId(Long id) {
        User user = userRepository.findById(id).get();
        List<GameOrder> byUserUid = gameOrderRepository.findByUserUid(user.getUid());
        List<GameOrderRes> gameOrderResList = toGameOrderResList(byUserUid);
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
                        gameOrder.getStatus()
                ))
                .toList();
    }

}
