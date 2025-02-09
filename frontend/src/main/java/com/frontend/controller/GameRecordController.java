package com.frontend.controller;

import com.frontend.entity.game.GameRecord;
import com.frontend.service.GameRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameRecordController {

    @Autowired
    private GameRecordService gameRecordService;

    // 根据 userUid 和 status 查询
    @GetMapping("/game-records/{userUid}")
    public List<GameRecord> getGameRecords(
            @PathVariable String userUid) {
        return gameRecordService.getGameRecordsByUserUidAndStatus(userUid);
    }
}
