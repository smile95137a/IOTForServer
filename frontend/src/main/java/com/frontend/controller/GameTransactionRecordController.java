package com.frontend.controller;

import com.frontend.entity.transection.GameTransactionRecord;
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
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<GameTransactionRecord>>> getTransactionRecordsByUserId(@PathVariable Long userId) {
        List<GameTransactionRecord> records = transactionRecordService.getTransactionRecordsByUserId(userId);
        ApiResponse<List<GameTransactionRecord>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易日期範圍查詢交易記錄
    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<List<GameTransactionRecord>>> getTransactionRecordsByDateRange(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate) {
        List<GameTransactionRecord> records = transactionRecordService.getTransactionRecordsByDateRange(startDate, endDate);
        ApiResponse<List<GameTransactionRecord>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易類型查詢交易記錄
    @GetMapping("/type/{transactionType}")
    public ResponseEntity<ApiResponse<List<GameTransactionRecord>>> getTransactionRecordsByType(@PathVariable String transactionType) {
        List<GameTransactionRecord> records = transactionRecordService.getTransactionRecordsByType(transactionType);
        ApiResponse<List<GameTransactionRecord>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }

    // 根據交易金額查詢交易記錄
    @GetMapping("/amount")
    public ResponseEntity<ApiResponse<List<GameTransactionRecord>>> getTransactionRecordsByAmount(@RequestParam Integer amount) {
        List<GameTransactionRecord> records = transactionRecordService.getTransactionRecordsByAmount(amount);
        ApiResponse<List<GameTransactionRecord>> response = ResponseUtils.success(records);
        return ResponseEntity.ok(response);
    }
}
