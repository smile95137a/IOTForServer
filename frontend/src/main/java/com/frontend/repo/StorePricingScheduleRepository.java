package com.frontend.repo;

import com.frontend.entity.store.StorePricingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StorePricingScheduleRepository extends JpaRepository<StorePricingSchedule, Long> {
    List<StorePricingSchedule> findByStoreId(Long id);

    // 根据店铺 ID 和星期几查询优惠时段
    List<StorePricingSchedule> findByStoreIdAndDayOfWeek(Long storeId, String dayOfWeek);

}
