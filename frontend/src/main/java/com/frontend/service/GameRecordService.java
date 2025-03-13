package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<GameRecord> getGameRecordsByUserUidAndStatus(Long id) {
        // 1. 檢查使用者是否存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

        // 2. 查找與該使用者對應的所有遊戲記錄
        List<GameRecord> gameRecords = gameRecordRepository.findByUserUidAndStatus(user.getUid() , "STARTED");
        if (gameRecords.isEmpty()) {
            throw new NoSuchElementException("No game records found for user with UID: " + user.getUid());
        }

        // 3. 為每個遊戲記錄動態設定對應店家的 hint
        gameRecords.forEach(record -> {
            Long storeId = record.getStoreId();
            if (storeId != null) { // 🔥 判斷 storeId 是否為 null
                storeRepository.findById(storeId).ifPresent(store -> {
                    record.setHint(store.getHint());
                });
            }
        });

        return gameRecords;
    }



    public List<GameRecord> getGameRecordsByUserUid(Long id) {
        // 1. 檢查使用者是否存在
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found with id: " + id));

        // 2. 查找與該使用者對應的所有遊戲記錄
        List<GameRecord> gameRecords = gameRecordRepository.findByUserUid(user.getUid());
        if (gameRecords.isEmpty()) {
            throw new NoSuchElementException("No game records found for user with UID: " + user.getUid());
        }
        return gameRecords;
    }
}
