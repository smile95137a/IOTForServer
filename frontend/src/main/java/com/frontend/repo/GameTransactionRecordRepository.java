package com.frontend.repo;

import com.frontend.entity.transection.GameTransactionRecord;
import com.frontend.res.report.TransactionSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'DAY' THEN DATE_FORMAT(transaction_date, '%Y-%m-%d')
            WHEN :type = 'WEEK' THEN CONCAT(YEAR(transaction_date), '-', LPAD(WEEK(transaction_date), 2, '0'))
            WHEN :type = 'MONTH' THEN DATE_FORMAT(transaction_date, '%Y-%m')
            WHEN :type = 'YEAR' THEN CAST(YEAR(transaction_date) AS CHAR)
        END AS period,
        SUM(amount) AS total_amount
    FROM game_transaction_record
    WHERE transaction_date BETWEEN :startDate AND :endDate
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Object[]> getTotalConsumptionByPeriod(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'DAY' THEN DATE_FORMAT(transaction_date, '%Y-%m-%d')
            WHEN :type = 'WEEK' THEN CONCAT(YEAR(transaction_date), '-', LPAD(WEEK(transaction_date), 2, '0'))
            WHEN :type = 'MONTH' THEN DATE_FORMAT(transaction_date, '%Y-%m')
            WHEN :type = 'YEAR' THEN CAST(YEAR(transaction_date) AS CHAR)
        END AS period,
        SUM(amount) AS total_revenue
    FROM game_transaction_record
    WHERE store_name = :storeName AND transaction_date BETWEEN :startDate AND :endDate
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Object[]> getStoreRevenueByPeriod(
            @Param("type") String type,
            @Param("storeName") String storeName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'DAY' THEN DATE_FORMAT(transaction_date, '%Y-%m-%d')
            WHEN :type = 'WEEK' THEN CONCAT(YEAR(transaction_date), '-', LPAD(WEEK(transaction_date), 2, '0'))
            WHEN :type = 'MONTH' THEN DATE_FORMAT(transaction_date, '%Y-%m')
            WHEN :type = 'YEAR' THEN CAST(YEAR(transaction_date) AS CHAR)
        END AS period,
        SUM(amount) AS total_revenue
    FROM game_transaction_record
    WHERE vendor_name = :vendorName AND transaction_date BETWEEN :startDate AND :endDate
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Object[]> getVendorRevenueByPeriod(
            @Param("type") String type,
            @Param("vendorName") String vendorName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Admin 查詢所有店鋪的營收數據
    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'DAY' THEN DATE_FORMAT(transaction_date, '%Y-%m-%d')
            WHEN :type = 'WEEK' THEN CONCAT(YEAR(transaction_date), '-', LPAD(WEEK(transaction_date), 2, '0'))
            WHEN :type = 'MONTH' THEN DATE_FORMAT(transaction_date, '%Y-%m')
            WHEN :type = 'YEAR' THEN CAST(YEAR(transaction_date) AS CHAR)
        END AS period,
        SUM(amount) AS total_revenue
    FROM game_transaction_record
    WHERE transaction_date BETWEEN :startDate AND :endDate
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Object[]> getStoreRevenueByPeriodForAdmin(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    // Admin 查詢所有廠商的營收數據
    @Query(value = """
    SELECT 
        CASE 
            WHEN :type = 'DAY' THEN DATE_FORMAT(transaction_date, '%Y-%m-%d')
            WHEN :type = 'WEEK' THEN CONCAT(YEAR(transaction_date), '-', LPAD(WEEK(transaction_date), 2, '0'))
            WHEN :type = 'MONTH' THEN DATE_FORMAT(transaction_date, '%Y-%m')
            WHEN :type = 'YEAR' THEN CAST(YEAR(transaction_date) AS CHAR)
        END AS period,
        SUM(amount) AS total_revenue
    FROM game_transaction_record
    WHERE transaction_date BETWEEN :startDate AND :endDate
    GROUP BY period
    ORDER BY period
""", nativeQuery = true)
    List<Object[]> getVendorRevenueByPeriodForAdmin(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);



}
