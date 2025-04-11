package com.frontend.repo;

import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.transection.UserTransactionsRes;
import com.frontend.entity.user.User;
import com.frontend.res.report.TransactionSummary;
import com.frontend.res.transaction.TransactionsRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRecordRepository extends JpaRepository<TransactionRecord, Long> {

    // 根據使用者查詢交易紀錄
    List<TransactionRecord> findByUser(User user);

    // 也可以透過 userId 來查詢
    List<TransactionRecord> findByUserId(Long userId);


    @Query("SELECT new com.frontend.res.transaction.TransactionsRes( " +
            "CAST(COALESCE(SUM(t.amount), 0) AS BigDecimal), " +
            "CAST(COUNT(t) AS Integer)) " +
            "FROM TransactionRecord t " +
            "WHERE t.transactionType = 'DEPOSIT' " +
            "AND FUNCTION('DATE', t.transactionDate) = CURRENT_DATE")
    TransactionsRes getTodayTotalDeposits();







    @Query("SELECT " +
            "  CASE WHEN :type = 'DAY' THEN CAST(DATE(t.transactionDate) AS string) " +
            "       WHEN :type = 'WEEK' THEN CONCAT(YEAR(t.transactionDate), '-', WEEK(t.transactionDate)) " +
            "       WHEN :type = 'MONTH' THEN CONCAT(YEAR(t.transactionDate), '-', MONTH(t.transactionDate)) " +
            "       WHEN :type = 'YEAR' THEN CAST(YEAR(t.transactionDate) AS string) " +
            "  END AS period, " +
            "  SUM(t.amount) AS total_deposit " +
            "FROM TransactionRecord t " +
            "WHERE t.transactionType = 'DEPOSIT' AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period ")
    List<Object[]> getTotalDepositAmountByPeriod(
            @Param("type") String type,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.frontend.entity.transection.UserTransactionsRes( " +
            "CAST(COALESCE(SUM(t.amount), 0) AS BigDecimal), " +
            "CAST(COUNT(t) AS Integer)) " +
            "FROM TransactionRecord t " +
            "WHERE t.transactionType = 'DEPOSIT' " +
            "AND t.user.id = :id")
    UserTransactionsRes getTotalDepositsAmountAndCount(@Param("id") Long id);

}