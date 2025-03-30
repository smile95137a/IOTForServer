package com.frontend.repo;

import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
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

}

