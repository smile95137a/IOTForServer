package com.frontend.repo;

import com.frontend.entity.game.GameOrder;
import com.frontend.entity.game.GameRecord;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vo.GameVO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GameRecordRepository extends CrudRepository<GameRecord, Long> {

    GameRecord findByGameId(String uid);

    List<GameRecord> findByUserUidAndStatus(String userUid, String status);

    @Query("SELECT g.gameId FROM GameRecord g WHERE g.storeId = :storeId AND g.status = :status")
    List<String> findGameIdByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") String status);

    // 查找根据 poolTableId 和 status 查询的所有 gameId
    @Query("SELECT g.gameId FROM GameRecord g WHERE g.poolTableId = :poolTableId AND g.status = :status")
    List<String> findGameIdByPoolTableIdAndStatus(@Param("poolTableId") Long poolTableId, @Param("status") String status);

    List<GameRecord> findByStoreId(Long storeId);

    Optional<List<GameRecord>> findByPoolTableId(Long poolTableId);

    @Query(value = "SELECT gr.game_id, gr.status FROM game_record gr WHERE gr.store_id = :storeId AND gr.pool_table_id = :poolTableId", nativeQuery = true)
    List<Object[]> findGameIdsByStoreIdStatusAndPoolTableId(@Param("storeId") Long storeId, @Param("poolTableId") Long poolTableId);

    @Query("SELECT go FROM GameRecord go WHERE go.gameId IN :allGameIds")
    List<GameRecord> findByGameIds(List<String> allGameIds);

    List<GameRecord> findByUserUid(String uid);
}
