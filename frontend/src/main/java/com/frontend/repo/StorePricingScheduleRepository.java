package com.frontend.repo;

import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.res.store.StorePricingScheduleRes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface StorePricingScheduleRepository extends JpaRepository<StorePricingSchedule, Long> {
    List<StorePricingSchedule> findByStoreId(Long id);

    // 根据店铺 ID 和星期几查询优惠时段
    @Query("SELECT s FROM StorePricingSchedule s WHERE s.store.id = :storeId AND s.dayOfWeek = :dayOfWeek")
    List<StorePricingSchedule> findByStoreIdAndDayOfWeek(@Param("storeId") Long storeId,
                                                         @Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT DISTINCT s FROM StorePricingSchedule s " +
            "LEFT JOIN FETCH s.timeSlots " +
            "WHERE s.store.id = :storeId AND s.dayOfWeek = :dayOfWeek")
    Optional<StorePricingSchedule> findScheduleWithMergedTimeSlots(@Param("storeId") Long storeId, @Param("dayOfWeek") String dayOfWeek);

    @Query("SELECT s FROM StorePricingSchedule s LEFT JOIN FETCH s.timeSlots WHERE s.store.id = :storeId AND s.dayOfWeek = :dayOfWeek")
    Optional<StorePricingSchedule> findScheduleWithTimeSlots(@Param("storeId") Long storeId, @Param("dayOfWeek") String dayOfWeek);

}
