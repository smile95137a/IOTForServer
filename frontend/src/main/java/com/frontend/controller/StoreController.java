package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.vendor.Vendor;
import com.frontend.res.store.StoreRes;
import com.frontend.service.StoreService;
import com.frontend.utils.RandomUtils;
import com.frontend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;


    // Get a store by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<List<StoreRes>>> countAvailableAndInUseByUid(@PathVariable String uid) {
        List<StoreRes> storeRes = storeService.countAvailableAndInUseByUid(uid).orElse(Collections.emptyList());
        if (storeRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "無此桌台", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StoreRes>>> findAll() {
        List<StoreRes> storeRes = storeService.findAll();
        if (storeRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "查無桌台", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }



}
