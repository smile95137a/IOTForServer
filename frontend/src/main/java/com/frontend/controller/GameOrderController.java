package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.game.GameRecord;
import com.frontend.res.game.GameOrderRes;
import com.frontend.service.GameOrderService;
import com.frontend.service.GameRecordService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameOrderController {

    @Autowired
    private GameOrderService gameOrderService;

    // 根据 userUid 和 status 查询
    @GetMapping("/game-order")
    public ResponseEntity<ApiResponse<List<GameOrderRes>>> getGameRecords() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        return ResponseEntity.ok(ResponseUtils.success(200, "查詢成功", gameOrderService.getOrderByUserId(id)));
    }
}
