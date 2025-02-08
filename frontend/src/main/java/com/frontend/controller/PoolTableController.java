package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.res.poolTable.PoolTableRes;
import com.frontend.res.store.StoreRes;
import com.frontend.service.PoolTableService;
import com.frontend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/portable")
public class PoolTableController {

    @Autowired
    private PoolTableService poolTableService;

    @GetMapping("/store/{storeUid}")
    public ResponseEntity<ApiResponse<List<PoolTable>>> countAvailableAndInUseByUid(@PathVariable String storeUid) {
        List<PoolTable> poolTables = poolTableService.findByStoreUid(storeUid);
        if (poolTables.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "無此桌台", null));
        }
        return ResponseEntity.ok(ResponseUtils.success(poolTables));
    }

    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<PoolTableRes>> getPoolTableById(@PathVariable String uid) {
        PoolTableRes poolTable = poolTableService.getPoolTableById(uid);
        if (poolTable != null) {
            ApiResponse<PoolTableRes> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<PoolTableRes> success = ResponseUtils.success(poolTable);
        return ResponseEntity.ok(success);
    }
}
