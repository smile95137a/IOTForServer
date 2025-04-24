package com.frontend.repo;

import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import com.frontend.entity.transection.UserTransactionsRes;
import com.frontend.res.HourlyRevenueDto;
import com.frontend.res.transaction.TransactionsRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface GameOrderRepository extends JpaRepository<GameOrder, Long> {
    GameOrder findByGameId(String gameId);

    List<GameOrder> findByUserId(String uid);

    List<GameOrder> findByGameIdInAndStartTimeBetween(List<String> gameIds, LocalDateTime startOfDay, LocalDateTime  endOfDay);

    List<GameOrder> findByGameIdAndStartTimeBetween(String gameId, LocalDateTime startOfDay, LocalDateTime endOfDay);

    @Query(value = "SELECT user_id, start_time, end_time FROM game_order WHERE game_id IN :gameIds AND start_time BETWEEN :startTime AND :endTime", nativeQuery = true)
    List<Object[]> findBookingTimesByGameIdsAndDateNative(
            @Param("gameIds") List<String> gameIds,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT go FROM GameOrder go WHERE go.gameId IN :gameIds")
    List<GameOrder> findByGameIds(@Param("gameIds") List<String> gameIds);


    @Query("SELECT new com.frontend.res.transaction.TransactionsRes( " +
            "CAST(COALESCE(SUM(g.totalPrice), 0) AS BigDecimal), " +
            "CAST(COUNT(g) AS Integer)) " +
            "FROM GameOrder g " +
            "JOIN PoolTable pt ON g.poolTableUid = pt.uid " +
            "JOIN pt.store s " +
            "JOIN s.vendor v " +
            "WHERE g.status = 'IS_PAY' " +
            "AND FUNCTION('DATE', g.startTime) = CURRENT_DATE " +
            "AND (:highestRoleId = 1 OR " +  // 超级管理员可以看所有
            "    (:highestRoleId = 2 AND v.id = :vendorId) OR " +  // 加盟商只看自己的
            "    (:highestRoleId = 5 AND s.id = :storeId))")  // 店家只看自己的
    TransactionsRes getTodayTotalConsumption(
            @Param("highestRoleId") Long highestRoleId,
            @Param("vendorId") Long vendorId,
            @Param("storeId") Long storeId);

    @Query("SELECT new com.frontend.res.transaction.TransactionsRes( " +
            "CAST(COALESCE(SUM(g.totalPrice), 0) AS BigDecimal), " +
            "CAST(COUNT(g) AS Integer)) " +
            "FROM GameOrder g " +
            "JOIN PoolTable pt ON g.poolTableUid = pt.uid " +
            "JOIN pt.store s " +
            "JOIN s.vendor v " +
            "WHERE g.status = 'IS_PAY' " +
            "AND FUNCTION('MONTH', g.startTime) = FUNCTION('MONTH', CURRENT_DATE) " +
            "AND FUNCTION('YEAR', g.startTime) = FUNCTION('YEAR', CURRENT_DATE) " +
            "AND (:highestRoleId = 1 OR " +  // 超级管理员可以看所有
            "    (:highestRoleId = 2 AND v.id = :vendorId) OR " +  // 加盟商只看自己的
            "    (:highestRoleId = 5 AND s.id = :storeId))")  // 店家只看自己的
    TransactionsRes getMonthTotalConsumption(
            @Param("highestRoleId") Long highestRoleId,
            @Param("vendorId") Long vendorId,
            @Param("storeId") Long storeId);


    @Query("SELECT new com.frontend.entity.transection.UserTransactionsRes( " +
            "CAST(COALESCE(SUM(g.totalPrice), 0) AS BigDecimal), " +
            "CAST(COUNT(g) AS Integer)) " +
            "FROM GameOrder g " +
            "WHERE g.status = 'IS_PAY' " +
            "AND g.userId = :uid")
    UserTransactionsRes getTotalConsumptionAmountAndCount(@Param("uid") String uid);


    @Query("SELECT new com.frontend.res.transaction.TransactionsRes( " +
            "CAST(COALESCE(SUM(g.totalPrice), 0) AS BigDecimal), " +
            "CAST(COUNT(g) AS Integer)) " +
            "FROM GameOrder g " +
            "JOIN PoolTable pt ON g.poolTableUid = pt.uid " +
            "JOIN pt.store s " +
            "WHERE g.status = 'IS_PAY' " +
            "AND s.uid = :storeUid " +
            "AND FUNCTION('DATE', g.startTime) = CURRENT_DATE")
    TransactionsRes getStoreTodayConsumption(@Param("storeUid") String storeUid);

}



