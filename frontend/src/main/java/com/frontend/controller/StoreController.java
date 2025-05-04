package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.res.store.StoreRes;
import com.frontend.service.StoreService;
import com.frontend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    // Get all stores with pricing and time slots
    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreRes>>> findAll() {
        List<StoreRes> storeRes = storeService.findAll();
        if (storeRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "查無店家", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }

    // Get store by UID, with available pool table count and pricing
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<List<StoreRes>>> countAvailableAndInUseByUid(@PathVariable String uid) {
        List<StoreRes> storeRes = storeService.countAvailableAndInUseByUid(uid);
        if (storeRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "無此店家", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }
}
