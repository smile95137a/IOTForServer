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

    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(g.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(g.transactionDate), '-', WEEK(g.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(g.transactionDate), '-', MONTH(g.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(g.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(g.amount) AS total_amount " +
            "FROM GameTransactionRecord g " +
            "WHERE g.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ")
    List<Object[]> getTotalConsumptionByPeriod(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(g.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(g.transactionDate), '-', WEEK(g.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(g.transactionDate), '-', MONTH(g.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(g.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(g.amount) AS total_revenue " +
            "FROM GameTransactionRecord g " +
            "WHERE g.storeName = :storeName AND g.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ")
    List<Object[]> getStoreRevenueByPeriod(
            @Param("type") String type,
            @Param("storeName") String storeName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(g.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(g.transactionDate), '-', WEEK(g.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(g.transactionDate), '-', MONTH(g.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(g.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(g.amount) AS total_revenue " +
            "FROM GameTransactionRecord g " +
            "WHERE g.vendorName = :vendorName AND g.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ")
    List<Object[]> getVendorRevenueByPeriod(
            @Param("type") String type,
            @Param("vendorName") String vendorName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);


    // Admin 查询所有店铺或厂商的营收数据
    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(g.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(g.transactionDate), '-', WEEK(g.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(g.transactionDate), '-', MONTH(g.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(g.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(g.amount) AS total_revenue " +
            "FROM GameTransactionRecord g " +
            "WHERE g.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period")
    List<Object[]> getStoreRevenueByPeriodForAdmin(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(g.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(g.transactionDate), '-', WEEK(g.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(g.transactionDate), '-', MONTH(g.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(g.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(g.amount) AS total_revenue " +
            "FROM GameTransactionRecord g " +
            "WHERE g.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period")
    List<Object[]> getVendorRevenueByPeriodForAdmin(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);



}
