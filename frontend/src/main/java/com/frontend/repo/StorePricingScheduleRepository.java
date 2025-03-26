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
    List<StorePricingScheduleRes> findByStoreIdAndDayOfWeek(Long storeId, String dayOfWeek);

    @Query("SELECT s FROM StorePricingSchedule s " +
            "LEFT JOIN FETCH s.regularTimeSlots " +
            "LEFT JOIN FETCH s.discountTimeSlots " +
            "WHERE s.store.id = :storeId")
    List<StorePricingSchedule> findByStoreIdWithSlots(@Param("storeId") Long storeId);


    @Query("SELECT s FROM StorePricingSchedule s " +
            "LEFT JOIN FETCH s.regularTimeSlots " +
            "LEFT JOIN FETCH s.discountTimeSlots " +
            "WHERE s.store.id = :storeId AND s.dayOfWeek = :dayOfWeek")
    List<StorePricingSchedule> findScheduleWithTimeSlots(
            @Param("storeId") Long storeId,
            @Param("dayOfWeek") String dayOfWeek
    );


}
