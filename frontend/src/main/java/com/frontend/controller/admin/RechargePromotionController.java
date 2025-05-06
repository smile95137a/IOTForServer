package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.req.recharge.RechargePromotionReq;
import com.frontend.res.recharge.RechargePromotionRes;
import com.frontend.service.RechargePromotionService;
import com.frontend.utils.ResponseUtils;
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
    public ResponseEntity<ApiResponse<RechargePromotionRes>> create(@RequestBody RechargePromotionReq req) {
        try {
            RechargePromotionRes res = promotionService.createPromotion(req);
            return ResponseEntity.ok(ResponseUtils.success(res));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RechargePromotionRes>> update(@PathVariable Long id, @RequestBody RechargePromotionReq req) {
        try {
            RechargePromotionRes res = promotionService.updatePromotion(id, req);
            return ResponseEntity.ok(ResponseUtils.success(res));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RechargePromotionRes>> get(@PathVariable Long id) {
        try {
            RechargePromotionRes res = promotionService.getPromotion(id);
            return ResponseEntity.ok(ResponseUtils.success(res));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<RechargePromotionRes>>> list() {
        try {
            List<RechargePromotionRes> list = promotionService.getAllPromotions();
            return ResponseEntity.ok(ResponseUtils.success(list));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            promotionService.deletePromotion(id);
            return ResponseEntity.ok(ResponseUtils.success(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
}
