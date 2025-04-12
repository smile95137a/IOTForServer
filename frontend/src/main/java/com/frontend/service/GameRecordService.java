package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.UserRepository;
import com.frontend.res.game.GameRecordRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<GameRecordRes> getGameRecordsByUserUidAndStatus(Long id) {
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
        Vendor vendor = store.getVendor();
        List<GameRecordRes> result = new ArrayList<>();
        started.forEach(game -> {
            GameRecordRes gameRecordRes = convertToGameRecordRes(game, vendor);
            result.add(gameRecordRes);
        });

        return result;
    }


    public List<GameRecordRes> getGameRecordsByUserUid(Long id) {
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


        Vendor vendor = store.getVendor();
        List<GameRecordRes> result = new ArrayList<>();
        allGameRecords.forEach(game -> {
            GameRecordRes gameRecordRes = convertToGameRecordRes(game, vendor);
            result.add(gameRecordRes);
        });
        return result;
    }


    public GameRecordRes convertToGameRecordRes(GameRecord gameRecord, Vendor vendor) {
        GameRecordRes res = new GameRecordRes();
        res.setId(gameRecord.getId());
        res.setGameId(gameRecord.getGameId());
        res.setStartTime(gameRecord.getStartTime());
        res.setUserUid(gameRecord.getUserUid());
        res.setPrice(gameRecord.getPrice());
        res.setStatus(gameRecord.getStatus());
        res.setStoreId(gameRecord.getStoreId());
        res.setStoreName(gameRecord.getStoreName());
        res.setVendorId(gameRecord.getVendorId());
        res.setVendorName(gameRecord.getVendorName());
        res.setContactInfo(gameRecord.getContactInfo());
        res.setPoolTableId(gameRecord.getPoolTableId());
        res.setPoolTableName(gameRecord.getPoolTableName());
        res.setRegularRateAmount(gameRecord.getRegularRateAmount());
        res.setDiscountRateAmount(gameRecord.getDiscountRateAmount());
        res.setHint(gameRecord.getHint());
        res.setVendor(vendor);
        return res;
    }

}
