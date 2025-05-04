package com.frontend.controller;

import com.frontend.entity.recharge.RechargePromotion;
import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.service.RechargeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeController {

    private final RechargeService rechargeService;

    @GetMapping("/standard")
    public ResponseEntity<List<RechargeStandard>> getStandard() {
        LocalDate today = LocalDate.now();
        return ResponseEntity.ok(rechargeService.getEffectiveRechargePlans(today));
    }

}