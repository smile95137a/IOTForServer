package com.frontend.repo;

import com.frontend.entity.store.SpecialDate;
import com.frontend.entity.store.SpecialTimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecialTimeSlotRepository extends JpaRepository<SpecialTimeSlot, Long> {
}
