package com.frontend.service;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.repo.RechargePromotionRepository;
import com.frontend.repo.RechargeStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RechargeService {
    private final RechargeStandardRepository standardRepo;
    private final RechargePromotionRepository promoRepo;

    public List<RechargeStandard> getStandardPlans() {
        return standardRepo.findAll();
    }

    public List<RechargePromotion> getActivePromotions(LocalDate date) {
        return promoRepo.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual("ACTIVE", date, date);
    }
}
