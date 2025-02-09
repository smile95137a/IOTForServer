package com.frontend.service;

import com.frontend.entity.game.GameRecord;
import com.frontend.repo.GameRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameRecordService {

    @Autowired
    private GameRecordRepository gameRecordRepository;

    public List<GameRecord> getGameRecordsByUserUidAndStatus(Long id) {
        return gameRecordRepository.findByIdAndStatus(id, "STARTED");
    }
}
