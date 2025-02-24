package com.frontend.repo;

import com.frontend.entity.game.GameRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface GameRecordRepository extends CrudRepository<GameRecord, Long> {

    GameRecord findByGameId(String uid);

    List<GameRecord> findByUserUidAndStatus(String userUid, String status);


}
