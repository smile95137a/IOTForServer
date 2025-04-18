package com.frontend.service;

import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.repo.GameTransactionRecordRepository;
import com.frontend.res.transaction.GameTransactionRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GameTransactionRecordService {

    @Autowired
    private GameTransactionRecordRepository transactionRecordRepository;

    // 根據用戶 ID 查詢交易記錄
    public List<GameTransactionRes> getTransactionRecordsByUserId(Long userId) {
        List<GameTransactionRecord> records = transactionRecordRepository.findByUserId(userId);
        return records.stream()
                .map(GameTransactionRecordService::convertToRes)
                .collect(Collectors.toList());
    }

    // 根據日期範圍查詢交易記錄
    public List<GameTransactionRes> getTransactionRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<GameTransactionRecord> records = transactionRecordRepository.findByTransactionDateBetween(startDate, endDate);
        return records.stream()
                .map(GameTransactionRecordService::convertToRes)
                .collect(Collectors.toList());
    }

    // 根據交易類型查詢交易記錄
    public List<GameTransactionRes> getTransactionRecordsByType(String transactionType) {
        List<GameTransactionRecord> records = transactionRecordRepository.findByTransactionType(transactionType);
        return records.stream()
                .map(GameTransactionRecordService::convertToRes)
                .collect(Collectors.toList());
    }

    // 根據交易金額查詢交易記錄
    public List<GameTransactionRes> getTransactionRecordsByAmount(Integer amount) {
        List<GameTransactionRecord> records = transactionRecordRepository.findByAmountGreaterThan(amount);
        return records.stream()
                .map(GameTransactionRecordService::convertToRes)
                .collect(Collectors.toList());
    }

    public static GameTransactionRes convertToRes(GameTransactionRecord gameTransactionRecord) {
        return GameTransactionRes.builder()
                .id(gameTransactionRecord.getId())
                .uid(gameTransactionRecord.getUid())
                .amount(gameTransactionRecord.getAmount())
                .vendorName(gameTransactionRecord.getVendorName())
                .storeName(gameTransactionRecord.getStoreName())
                .tableNumber(gameTransactionRecord.getTableNumber())
                .createdAt(gameTransactionRecord.getCreatedAt())
                .transactionDate(gameTransactionRecord.getTransactionDate())
                .transactionType(gameTransactionRecord.getTransactionType())
                .user(gameTransactionRecord.getUser())
                .build();
    }
}
