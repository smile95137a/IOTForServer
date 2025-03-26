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

    @Query("SELECT g FROM GameOrder g " +
            "WHERE g.poolTableUid = :poolTableUid " +
            "AND g.startTime BETWEEN :start AND :end")
    List<GameOrder> findByGameIdAndStartTimeBetweenWithBuffer(Long storeId, Long poolTableId, LocalDateTime localDateTime, LocalDateTime localDateTime1);
}

