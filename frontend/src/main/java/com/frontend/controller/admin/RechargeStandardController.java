package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.req.store.RechargeStandardReq;
import com.frontend.res.store.RechargeStandardRes;
import com.frontend.service.RechargeStandardService;
import com.frontend.utils.ResponseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b/recharge-standards")
@RequiredArgsConstructor
public class RechargeStandardController {

    private final RechargeStandardService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<RechargeStandardRes>>> list() {
        try {
            List<RechargeStandardRes> data = service.findAll();
            return ResponseEntity.ok(ResponseUtils.success(data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RechargeStandardRes>> get(@PathVariable Long id) {
        try {
            RechargeStandardRes data = service.findById(id);
            return ResponseEntity.ok(ResponseUtils.success(data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RechargeStandardRes>> create(@RequestBody RechargeStandardReq req) {
        try {
            RechargeStandardRes data = service.create(req);
            return ResponseEntity.ok(ResponseUtils.success(data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RechargeStandardRes>> update(@PathVariable Long id, @RequestBody RechargeStandardReq req) {
        try {
            RechargeStandardRes data = service.update(id, req);
            return ResponseEntity.ok(ResponseUtils.success(data));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            service.delete(id);
            return ResponseEntity.ok(ResponseUtils.success(null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
}
