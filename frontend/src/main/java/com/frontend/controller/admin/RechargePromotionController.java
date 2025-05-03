package com.frontend.controller.admin;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.req.recharge.RechargePromotionReq;
import com.frontend.res.recharge.RechargePromotionRes;
import com.frontend.service.RechargePromotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/recharge/promotion")
@RequiredArgsConstructor
public class RechargePromotionController {

    private final RechargePromotionService promotionService;

    @PostMapping
    public ResponseEntity<RechargePromotionRes> create(@RequestBody RechargePromotionReq req) {
        return ResponseEntity.ok(promotionService.createPromotion(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RechargePromotionRes> update(@PathVariable Long id, @RequestBody RechargePromotionReq req) {
        return ResponseEntity.ok(promotionService.updatePromotion(id, req));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RechargePromotionRes> get(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getPromotion(id));
    }

    @GetMapping
    public ResponseEntity<List<RechargePromotionRes>> list() {
        return ResponseEntity.ok(promotionService.getAllPromotions());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        promotionService.deletePromotion(id);
        return ResponseEntity.noContent().build();
    }
}