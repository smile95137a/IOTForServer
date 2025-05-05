package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.store.Store;
import com.frontend.repo.StoreRepository;
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

    @Autowired
    private StoreRepository storeRepository;

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
    public ResponseEntity<ApiResponse<StoreRes>> countAvailableAndInUseByUid(@PathVariable String uid) {
        Store store = storeRepository.findByUid(uid).get();
        StoreRes storeRes = storeService.findById(store.getId());
        if (storeRes == null) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "無此店家", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }
}
