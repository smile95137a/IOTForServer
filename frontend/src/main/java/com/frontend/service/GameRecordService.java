package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<GameRecord> getGameRecordsByUserUidAndStatus(Long id) {
        // 安全地获取 User 对象
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 获取状态为 "STARTED" 的游戏记录
        List<GameRecord> started = gameRecordRepository.findByUserUidAndStatus(user.getUid(), "STARTED");

        // 如果没有找到任何游戏记录，返回空列表或抛出异常
        if (started.isEmpty()) {
            return Collections.emptyList(); // 或者抛出异常
        }

        // 假设每个游戏记录的 storeId 都相同，取第一个记录的 storeId
        Long storeId = started.get(0).getStoreId();

        // 安全地获取 Store 对象
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        // 设置所有游戏记录的提示信息
        started.forEach(x -> x.setHint(store.getHint()));

        return started;
    }


    public List<GameRecord> getGameRecordsByUserUid(Long id) {
        // 安全地获取 User 对象
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // 获取该用户的所有 GameRecord
        List<GameRecord> allGameRecords = gameRecordRepository.findByUserUid(user.getUid());

        // 如果没有找到任何记录，返回空列表
        if (allGameRecords.isEmpty()) {
            return Collections.emptyList(); // 或者抛出异常，依据需求决定
        }

        // 假设所有记录的 storeId 相同，获取第一个记录的 storeId
        Long storeId = allGameRecords.get(0).getStoreId();

        // 安全地获取 Store 对象
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        // 设置所有 GameRecord 的提示信息
        allGameRecords.forEach(x -> x.setHint(store.getHint()));

        return allGameRecords;
    }

}
