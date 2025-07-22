package com.frontend.repo;

import com.frontend.entity.store.StorePricingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StorePricingScheduleRepository extends JpaRepository<StorePricingSchedule, Long> {
    List<StorePricingSchedule> findByStoreId(Long id);

    Optional<Object> findByStoreIdAndDayOfWeek(Long storeId, String upperCase);

    // StorePricingScheduleRepository.java
    @Modifying
    @Query("DELETE FROM StorePricingSchedule s WHERE s.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}
