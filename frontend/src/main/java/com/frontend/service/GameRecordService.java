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

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<GameRecord> getGameRecordsByUserUidAndStatus(Long id) {
        User user = userRepository.findById(id).get();
        List<GameRecord> started = gameRecordRepository.findByUserUid(user.getUid());
        GameRecord gameRecord = started.get(0);
        Long storeId = gameRecord.getStoreId();
        Store store = storeRepository.findById(storeId).get();
        started.forEach(x -> x.setHint(store.getHint()));
        return started;
    }
}
