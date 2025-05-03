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

    public RechargePromotionRes createPromotion(RechargePromotionReq req) {
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

    public void deletePromotion(Long id) {
        promotionRepository.deleteById(id);
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

    public RechargePromotionRes updatePromotion(Long id, RechargePromotionReq req) {
        RechargePromotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promotion not found"));

        promotion.setName(req.getName());
        promotion.setStartDate(req.getStartDate());
        promotion.setEndDate(req.getEndDate());
        promotion.setUpdateTime(LocalDateTime.now());

        // 先清空舊的 details
        promotion.getDetails().clear();
        detailRepository.deleteAllByPromotion(promotion); // 你可以自己新增這個方法

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

        promotion.getDetails().addAll(newDetails);

        RechargePromotion updated = promotionRepository.save(promotion);
        return toRes(updated);
    }

}
