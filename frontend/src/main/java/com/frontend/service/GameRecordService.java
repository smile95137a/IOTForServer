package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.GameRecordRepository;
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

    public List<GameRecord> getGameRecordsByUserUidAndStatus(Long id) {
        User user = userRepository.findById(id).get();
        return gameRecordRepository.findByUserUidAndStatus(user.getUid(), "STARTED");
    }
}
