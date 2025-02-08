package com.frontend.service;

import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.repo.GameTransactionRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GameTransactionRecordService {

    @Autowired
    private GameTransactionRecordRepository transactionRecordRepository;

    // 根據用戶 ID 查詢所有交易記錄
    public List<GameTransactionRecord> getTransactionRecordsByUserId(Long userId) {
        return transactionRecordRepository.findByUserId(userId);
    }

    // 根據日期範圍查詢交易記錄
    public List<GameTransactionRecord> getTransactionRecordsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return transactionRecordRepository.findByTransactionDateBetween(startDate, endDate);
    }

    // 根據交易類型查詢交易記錄
    public List<GameTransactionRecord> getTransactionRecordsByType(String transactionType) {
        return transactionRecordRepository.findByTransactionType(transactionType);
    }

    // 根據交易金額查詢交易記錄
    public List<GameTransactionRecord> getTransactionRecordsByAmount(Integer amount) {
        return transactionRecordRepository.findByAmountGreaterThan(amount);
    }
}
