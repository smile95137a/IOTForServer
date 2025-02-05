package src.main.java.backend.controller;

import src.main.java.backend.config.message.ApiResponse;
import src.main.java.backend.entity.poolTable.PoolTable;
import src.main.java.backend.service.PoolTableService;
import src.main.java.backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/poolTables")
public class PoolTableController {

    @Autowired
    private PoolTableService poolTableService;

    // 创建一个新的桌台
    @PostMapping
    public ResponseEntity<ApiResponse<PoolTable>> createPoolTables(@RequestBody PoolTable poolTable) {
        try {
            // 呼叫 Service 處理多個 PoolTable 的創建
            PoolTable createdPoolTables = poolTableService.createPoolTables(poolTable);
            ApiResponse<PoolTable> success = ResponseUtils.success(createdPoolTables);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            // 處理錯誤並返回
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }


    // 根据 ID 获取桌台
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<PoolTable>> getPoolTableById(@PathVariable String uid) {
        Optional<PoolTable> poolTable = poolTableService.getPoolTableById(uid);
        if (poolTable.isEmpty()) {
            ApiResponse<PoolTable> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<PoolTable> success = ResponseUtils.success(poolTable.get());
        return ResponseEntity.ok(success);
    }

    // 获取所有桌台
    @GetMapping
    public ResponseEntity<ApiResponse<List<PoolTable>>> getAllPoolTables() {
        List<PoolTable> poolTables = poolTableService.getAllPoolTables();
        ApiResponse<List<PoolTable>> success = ResponseUtils.success(poolTables);
        return ResponseEntity.ok(success);
    }

    // 更新桌台
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<PoolTable>> updatePoolTable(@PathVariable String uid, @RequestBody PoolTable updatedPoolTable) {
        try {
            PoolTable poolTable = poolTableService.updatePoolTable(uid, updatedPoolTable);
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
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }
}
