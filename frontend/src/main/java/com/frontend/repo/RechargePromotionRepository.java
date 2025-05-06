package com.frontend.repo;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.enums.PromotionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface RechargePromotionRepository extends JpaRepository<RechargePromotion, Long> {
    List<RechargePromotion> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(PromotionStatus status, LocalDate start, LocalDate end);

    List<RechargePromotion> findByStartDateBeforeAndEndDateAfterAndIdNot(LocalDateTime endDate, LocalDateTime startDate, Long id);

    List<RechargePromotion> findByStartDateBeforeAndEndDateAfter(LocalDateTime endDate, LocalDateTime startDate);
}