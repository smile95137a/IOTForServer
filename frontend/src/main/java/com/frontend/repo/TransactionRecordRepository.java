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
            "CAST(COUNT(t) AS Integer), " +
            "s.id) " +  // 加入店铺 ID 作为查询的一部分
            "FROM TransactionRecord t " +
            "JOIN t.user u " +
            "LEFT JOIN u.stores s " +
            "LEFT JOIN s.vendor v " +
            "WHERE t.transactionType = 'DEPOSIT' " +
            "AND FUNCTION('DATE', t.transactionDate) = CURRENT_DATE " +
            "AND (:highestRoleId = 1 OR " +  // 超级管理员可以看所有
            "    (:highestRoleId = 2 AND v.id = :vendorId) OR " +  // 加盟商只看自己的
            "    (:highestRoleId = 5 AND s.id IN :storeIds)) " +  // 店家只看自己的店铺
            "GROUP BY s.id")  // 按照店铺 ID 分组
    List<TransactionsRes> getTodayTotalDeposits(
            @Param("highestRoleId") Long highestRoleId,
            @Param("vendorId") Long vendorId,
            @Param("storeIds") List<Long> storeIds);








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

    @Query("SELECT " +
            "CASE WHEN :type = 'DAY' THEN CAST(DATE(t.transactionDate) AS string) " +
            "     WHEN :type = 'WEEK' THEN CONCAT(YEAR(t.transactionDate), '-', WEEK(t.transactionDate)) " +
            "     WHEN :type = 'MONTH' THEN CONCAT(YEAR(t.transactionDate), '-', MONTH(t.transactionDate)) " +
            "     WHEN :type = 'YEAR' THEN CAST(YEAR(t.transactionDate) AS string) " +
            "END AS period, " +
            "COUNT(t) AS deposit_count " +
            "FROM TransactionRecord t " +
            "WHERE t.transactionType = 'DEPOSIT' AND t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period")
    List<Object[]> getDepositCountByPeriod(@Param("type") String type,
                                           @Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "CASE WHEN :type = 'DAY' THEN CAST(DATE(t.transactionDate) AS string) " +
            "     WHEN :type = 'WEEK' THEN CONCAT(YEAR(t.transactionDate), '-', WEEK(t.transactionDate)) " +
            "     WHEN :type = 'MONTH' THEN CONCAT(YEAR(t.transactionDate), '-', MONTH(t.transactionDate)) " +
            "     WHEN :type = 'YEAR' THEN CAST(YEAR(t.transactionDate) AS string) " +
            "END AS period, " +
            "COUNT(t) AS consumption_count " +
            "FROM GameTransactionRecord t " +
            "WHERE t.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY period")
    List<Object[]> getConsumptionCountByPeriod(@Param("type") String type,
                                               @Param("startDate") LocalDateTime startDate,
                                               @Param("endDate") LocalDateTime endDate);

    @Query("SELECT " +
            "CASE WHEN :type = 'DAY' THEN CAST(DATE(u.createTime) AS string) " +
            "     WHEN :type = 'WEEK' THEN CONCAT(YEAR(u.createTime), '-', WEEK(u.createTime)) " +
            "     WHEN :type = 'MONTH' THEN CONCAT(YEAR(u.createTime), '-', MONTH(u.createTime)) " +
            "     WHEN :type = 'YEAR' THEN CAST(YEAR(u.createTime) AS string) " +
            "END AS period, " +
            "COUNT(u) AS user_count " +
            "FROM User u " +
            "WHERE u.createTime BETWEEN :startDate AND :endDate " +
            "GROUP BY period")
    List<Object[]> getUserCountByPeriod(@Param("type") String type,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.frontend.res.transaction.TransactionsRes( " +
            "CAST(COALESCE(SUM(t.amount), 0) AS BigDecimal), " +
            "CAST(COUNT(t) AS Integer)) " +
            "FROM TransactionRecord t " +
            "JOIN t.user u " +
            "JOIN u.stores s " +  // Changed from u.store to u.stores
            "WHERE t.transactionType = 'DEPOSIT' " +
            "AND s.uid = :storeUid " +
            "AND FUNCTION('DATE', t.transactionDate) = CURRENT_DATE")
    TransactionsRes getStoreTodayDeposits(@Param("storeUid") String storeUid);

}