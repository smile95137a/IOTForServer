package backend.repo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import backend.entity.menu.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {

    @Query("SELECT m FROM Menu m WHERE m.endDate >= CURRENT_DATE ORDER BY m.startDate ASC")
    List<Menu> findActiveMenusSortedByStartDate();

    @Query("SELECT m FROM Menu m WHERE m.endDate < CURRENT_DATE ORDER BY m.startDate ASC")
    List<Menu> findExpiredMenusSortedByStartDate();
    
	@Query("SELECT m FROM Menu m WHERE m.isEnable = true AND m.channelId = :channelId AND m.uid NOT IN :uids")
	List<Menu> findByChannelIdAndIsEnableTrueAndUidNotIn(@Param("channelId") Long channelId, @Param("uids") List<String> uids);

	
    Optional<Menu> findByuid(String uid);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Menu m " +
            "WHERE (m.startDate <= :endDate AND m.endDate >= :startDate) AND m.channelId = :channelId ")
     boolean existsByDateRangeOverlap(@Param("startDate") Date startDate,
                                      @Param("endDate") Date endDate,
                                      @Param("channelId") Long channelId);
    

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
           "FROM Menu m " +
           "WHERE (m.startDate <= :endDate AND m.endDate >= :startDate) " +
           "AND m.id != :id AND m.channelId = :channelId")
    boolean existsByDateRangeOverlapExcludingId(@Param("startDate") Date startDate,
                                                @Param("endDate") Date endDate,
                                                @Param("id") Long id,
                                                @Param("channelId") Long channelId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Menu m " +
            "WHERE :date BETWEEN m.startDate AND m.endDate AND m.channelId = :channelId ")
     boolean existsByDateWithinRange(@Param("date") Date date, @Param("channelId") Long channelId);
    
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END " +
            "FROM Menu m WHERE :date BETWEEN m.startDate AND m.endDate " +
            "AND m.id != :id AND m.channelId = :channelId")
     boolean existsByDateWithinRangeExcludingId(Date date, Long id, @Param("channelId") Long channelId);

    @Query("SELECT m FROM Menu m WHERE m.startDate = :dateTime AND m.channelId = :channelId")
    Optional<Menu> findMenuByStartDateAndChannelId(@Param("dateTime") LocalDateTime dateTime, @Param("channelId") Long channelId);

    @Query("SELECT m FROM Menu m WHERE m.endDate = :dateTime AND m.channelId = :channelId")
    Optional<Menu> findMenuByEndDateAndChannelId(@Param("dateTime") LocalDateTime dateTime, @Param("channelId") Long channelId);

    
}
