package com.frontend.controller.admin;

import java.util.List;
import java.util.Optional;

import com.frontend.res.poolTable.AdminPoolTableRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;

@RestController
@RequestMapping("/api/b/poolTables")
public class AdminPoolTableController {

    @Autowired
    private AdminPoolTableService poolTableService;

    // 创建一个新的桌台
    @PostMapping
    public ResponseEntity<ApiResponse<PoolTable>> createPoolTables(@RequestBody PoolTableReq poolTableReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            // 呼叫 Service 處理多個 PoolTable 的創建
            PoolTable createdPoolTables = poolTableService.createPoolTable(poolTableReq , id);
            ApiResponse<PoolTable> success = ResponseUtils.success(createdPoolTables);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            // 處理錯誤並返回
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<PoolTable>>> getPoolTablesByStoreId(@PathVariable Long storeId) {
        List<PoolTable> poolTables = poolTableService.findByStoreId(storeId);
        return ResponseEntity.ok(ResponseUtils.success(poolTables));
    }


    // 根据 ID 获取桌台
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<AdminPoolTableRes>> getPoolTableById(@PathVariable String uid) {
        Optional<AdminPoolTableRes> poolTable = poolTableService.getPoolTableById(uid);
        if (poolTable.isEmpty()) {
            ApiResponse<AdminPoolTableRes> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<AdminPoolTableRes> success = ResponseUtils.success(poolTable.get());
        return ResponseEntity.ok(success);
    }

    // 获取所有桌台
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminPoolTableRes>>> getAllPoolTables() {
        List<AdminPoolTableRes> poolTables = poolTableService.getAllPoolTables();
        ApiResponse<List<AdminPoolTableRes>> success = ResponseUtils.success(poolTables);
        return ResponseEntity.ok(success);
    }

    // 更新桌台
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<PoolTable>> updatePoolTable(@PathVariable String uid, @RequestBody PoolTableReq poolTableReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            PoolTable poolTable = poolTableService.updatePoolTable(uid, poolTableReq , id);
            ApiResponse<PoolTable> success = ResponseUtils.success(poolTable);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<PoolTable> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // 删除桌台
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deletePoolTable(@PathVariable String uid) {
        try {
            poolTableService.deletePoolTable(uid);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            e.printStackTrace();
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    @PostMapping("/closeTable")
    public ResponseEntity<ApiResponse<PoolTable>> closePoolTable(@RequestBody PoolTableReq poolTableReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            PoolTable poolTable = poolTableService.closePoolTable(poolTableReq , id);
            ApiResponse<PoolTable> success = ResponseUtils.success(poolTable);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<PoolTable> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
