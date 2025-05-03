package com.frontend.repo;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.entity.recharge.RechargePromotionDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RechargePromotionDetailRepository extends JpaRepository<RechargePromotionDetail, Long> {
    List<RechargePromotionDetail> findByPromotionId(Long promotionId);

    void deleteAllByPromotion(RechargePromotion promotion);
}