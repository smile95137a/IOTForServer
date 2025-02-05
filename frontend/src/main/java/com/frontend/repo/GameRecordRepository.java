package com.frontend.repo;

import com.frontend.entity.game.GameRecord;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameRecordRepository extends CrudRepository<GameRecord, Long> {

    GameRecord findByGameId(String uid);
}
