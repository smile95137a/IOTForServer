package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.game.GameRecord;
import com.frontend.service.GameRecordService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    @GetMapping("/game-records")
    public ResponseEntity<ApiResponse<List<GameRecord>>> getGameRecords() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        return ResponseEntity.ok(ResponseUtils.success(200, "開台成功", gameRecordService.getGameRecordsByUserUidAndStatus(id)));
    }
}
