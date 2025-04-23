package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.game.GameRecord;
import com.frontend.req.game.CheckoutReq;
import com.frontend.res.game.GameOrderRes;
import com.frontend.res.game.GameRes;
import com.frontend.service.GameOrderService;
import com.frontend.service.GameRecordService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @PostMapping("/gamePay")
    public ResponseEntity<ApiResponse<?>> checkout(@RequestBody CheckoutReq checkoutReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            GameRes checkout = gameOrderService.checkout(checkoutReq, id);
            return ResponseEntity.ok(ResponseUtils.success(checkout));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
}
