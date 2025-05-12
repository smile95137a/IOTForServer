package com.frontend.service;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.enums.PromotionStatus;
import com.frontend.repo.RechargePromotionRepository;
import com.frontend.repo.RechargeStandardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RechargeService {
    private final RechargeStandardRepository standardRepo;
    private final RechargePromotionRepository promoRepo;

    public List<RechargeStandard> getEffectiveRechargePlans(LocalDate date) {
        List<RechargePromotion> activePromotions =
                promoRepo.findByStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(PromotionStatus.AVAILABLE, date, date);

        if (!activePromotions.isEmpty()) {
            // 只取第一個活動（假設同時只會有一個）
            RechargePromotion promo = activePromotions.get(0);
            // 將活動轉成 RechargeStandard 格式（方便前端統一處理）
            List<RechargeStandard> promoPlans = promo.getDetails().stream()
                    .map(d -> {
                        RechargeStandard rs = new RechargeStandard();
                        rs.setId(d.getId());
                        rs.setRechargeAmount(d.getRechargeAmount());
                        rs.setBonusAmount(d.getBonusAmount());
                        rs.setStatus(promo.getStatus());
                        rs.setCreateTime(promo.getCreateTime());
                        rs.setUpdateTime(promo.getUpdateTime());
                        return rs;
                    })
                    .sorted(Comparator.comparing(RechargeStandard::getRechargeAmount))
                    .collect(Collectors.toList());
            return promoPlans;
        }

        // 如果沒活動，返回標準方案
        return standardRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(RechargeStandard::getRechargeAmount))
                .collect(Collectors.toList());
    }
}
