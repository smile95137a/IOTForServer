package com.frontend.repo;

import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.transection.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    // TimeSlotRepository.java
    @Modifying
    @Query("DELETE FROM TimeSlot t WHERE t.schedule.store.id = :storeId")
    void deleteByScheduleStoreId(@Param("storeId") Long storeId);
}
