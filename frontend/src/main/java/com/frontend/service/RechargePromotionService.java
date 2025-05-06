package com.frontend.service;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.entity.recharge.RechargePromotionDetail;
import com.frontend.enums.PromotionStatus;
import com.frontend.repo.RechargePromotionDetailRepository;
import com.frontend.repo.RechargePromotionRepository;
import com.frontend.req.recharge.RechargePromotionReq;
import com.frontend.res.recharge.RechargePromotionDetailRes;
import com.frontend.res.recharge.RechargePromotionRes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RechargePromotionService {

    private final RechargePromotionRepository promotionRepository;
    private final RechargePromotionDetailRepository detailRepository;

    public RechargePromotionService(RechargePromotionRepository promotionRepository,
                                    RechargePromotionDetailRepository detailRepository) {
        this.promotionRepository = promotionRepository;
        this.detailRepository = detailRepository;
    }

    public List<RechargePromotionRes> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public RechargePromotionRes getPromotion(Long id) {
        RechargePromotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));
        return toRes(promotion);
    }

    @Transactional
    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
    }

    public RechargePromotionRes createPromotion(RechargePromotionReq req) {
        // 檢查是否有時間重疊的促銷
        checkTimeOverlap(req.getStartDate().atStartOfDay(), req.getEndDate().atTime(23, 59, 59));


        RechargePromotion promotion = new RechargePromotion();
        promotion.setName(req.getName());
        promotion.setStartDate(req.getStartDate());
        promotion.setEndDate(req.getEndDate());
        promotion.setStatus(PromotionStatus.AVAILABLE);
        promotion.setCreateTime(LocalDateTime.now());
        promotion.setUpdateTime(LocalDateTime.now());

        List<RechargePromotionDetail> details = req.getDetails().stream().map(detailReq -> {
            RechargePromotionDetail detail = new RechargePromotionDetail();
            detail.setRechargeAmount(detailReq.getRechargeAmount());
            detail.setBonusAmount(detailReq.getBonusAmount());
            detail.setStatus(PromotionStatus.AVAILABLE);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setPromotion(promotion);
            return detail;
        }).collect(Collectors.toList());

        promotion.setDetails(details);
        RechargePromotion saved = promotionRepository.save(promotion);

        return toRes(saved);
    }

    @Transactional
    public RechargePromotionRes updatePromotion(Long id, RechargePromotionReq req) {
        RechargePromotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        // 檢查是否有時間重疊的促銷，並排除自己
        checkTimeOverlap(req.getStartDate().atStartOfDay(), req.getEndDate().atTime(23, 59, 59));
        promotion.setName(req.getName());
        promotion.setStartDate(req.getStartDate());
        promotion.setEndDate(req.getEndDate());
        promotion.setUpdateTime(LocalDateTime.now());

        // 先清空舊的 details
        promotion.getDetails().clear();

        // 刪除資料庫中的舊 details
        detailRepository.deleteAllByPromotion(promotion); // 確保此方法正確刪除資料庫中的 details

        // 加入新的 details
        List<RechargePromotionDetail> newDetails = req.getDetails().stream().map(detailReq -> {
            RechargePromotionDetail detail = new RechargePromotionDetail();
            detail.setRechargeAmount(detailReq.getRechargeAmount());
            detail.setBonusAmount(detailReq.getBonusAmount());
            detail.setStatus(PromotionStatus.AVAILABLE);
            detail.setCreateTime(LocalDateTime.now());
            detail.setUpdateTime(LocalDateTime.now());
            detail.setPromotion(promotion);
            return detail;
        }).collect(Collectors.toList());

        // 更新 promotion 的 details 集合
        promotion.setDetails(newDetails);

        // 保存更新後的 promotion
        RechargePromotion updated = promotionRepository.save(promotion);
        return toRes(updated);
    }

    private void checkTimeOverlap(LocalDateTime startDate, LocalDateTime endDate) {
        List<RechargePromotion> overlappingPromotions = promotionRepository.findByStartDateBeforeAndEndDateAfter(endDate, startDate);
        if (!overlappingPromotions.isEmpty()) {
            throw new RuntimeException("There are promotions with overlapping time ranges.");
        }
    }

    private void checkTimeOverlapForUpdate(RechargePromotion promotion, LocalDateTime startDate, LocalDateTime endDate) {
        List<RechargePromotion> overlappingPromotions = promotionRepository.findByStartDateBeforeAndEndDateAfterAndIdNot(endDate, startDate, promotion.getId());
        if (!overlappingPromotions.isEmpty()) {
            throw new RuntimeException("There are promotions with overlapping time ranges.");
        }
    }

    private RechargePromotionRes toRes(RechargePromotion promotion) {
        RechargePromotionRes res = new RechargePromotionRes();
        res.setId(promotion.getId());
        res.setName(promotion.getName());
        res.setStartDate(promotion.getStartDate());
        res.setEndDate(promotion.getEndDate());

        List<RechargePromotionDetailRes> detailResList = promotion.getDetails().stream().map(detail -> {
            RechargePromotionDetailRes detailRes = new RechargePromotionDetailRes();
            detailRes.setId(detail.getId());
            detailRes.setRechargeAmount(detail.getRechargeAmount());
            detailRes.setBonusAmount(detail.getBonusAmount());
            return detailRes;
        }).collect(Collectors.toList());

        res.setDetails(detailResList);
        return res;
    }

}
