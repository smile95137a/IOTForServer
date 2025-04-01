package com.frontend.repo;

import com.frontend.entity.store.TimeSlot;
import com.frontend.entity.transection.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
}
