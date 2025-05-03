package com.frontend.repo;

import com.frontend.entity.recharge.RechargePromotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RechargePromotionRepository extends JpaRepository<RechargePromotion, Long> {
    List<RechargePromotion> findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String status, LocalDate start, LocalDate end);
}