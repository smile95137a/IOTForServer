package com.frontend.repo;

import com.frontend.entity.store.StorePricingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface StorePricingScheduleRepository extends JpaRepository<StorePricingSchedule, Long> {
    List<StorePricingSchedule> findByStoreId(Long id);

}
