package com.frontend.controller;

import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.res.transaction.GameTransactionRes;
import com.frontend.service.GameTransactionRecordService;
import com.frontend.utils.ResponseUtils;
import com.frontend.config.message.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/transaction")
public class GameTransactionRecordController {

    @Autowired
    private GameTransactionRecordService transactionRecordService;

    // 根據用戶 ID 查詢所有交易記錄
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<GameTransactionRes>>> getTransactionRecordsByUserId() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        List<GameTransactionRes> records = transactionRecordService.getTransactionRecordsByUserId(userId);
        ApiResponse<List<GameTransactionRes>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易日期範圍查詢交易記錄
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<GameTransactionRes>>> getTransactionRecordsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<GameTransactionRes> records = transactionRecordService.getTransactionRecordsByDateRange(startDate, endDate);
        ApiResponse<List<GameTransactionRes>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易類型查詢交易記錄
    @GetMapping("/type/{transactionType}")
    public ResponseEntity<ApiResponse<List<GameTransactionRes>>> getTransactionRecordsByType(@PathVariable String transactionType) {
        List<GameTransactionRes> records = transactionRecordService.getTransactionRecordsByType(transactionType);
        ApiResponse<List<GameTransactionRes>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易金額查詢交易記錄
    @GetMapping("/amount")
    public ResponseEntity<ApiResponse<List<GameTransactionRes>>> getTransactionRecordsByAmount(@RequestParam Integer amount) {
        List<GameTransactionRes> records = transactionRecordService.getTransactionRecordsByAmount(amount);
        ApiResponse<List<GameTransactionRes>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }
}
