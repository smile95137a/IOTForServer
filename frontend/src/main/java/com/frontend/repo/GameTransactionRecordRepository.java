package com.frontend.repo;

import com.frontend.entity.transection.GameTransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameTransactionRecordRepository extends JpaRepository<GameTransactionRecord, Long> {

    // 根據用戶查詢所有交易記錄
    List<GameTransactionRecord> findByUserId(Long userId);

    // 根據日期範圍查詢交易記錄
    List<GameTransactionRecord> findByTransactionDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    // 根據交易類型查詢交易記錄
    List<GameTransactionRecord> findByTransactionType(String transactionType);

    // 根據交易金額查詢交易記錄
    List<GameTransactionRecord> findByAmountGreaterThan(Integer amount);
}
