package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.poolTable.PoolTable;
import backend.service.PoolTableService;
import backend.utils.ResponseUtils;
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
    public ResponseEntity<ApiResponse<PoolTable>> createPoolTable(@RequestBody PoolTable poolTable) {
        PoolTable createdPoolTable = poolTableService.createPoolTable(poolTable);
        ApiResponse<PoolTable> success = ResponseUtils.success(createdPoolTable);
        return ResponseEntity.ok(success);
    }

    // 根据 ID 获取桌台
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PoolTable>> getPoolTableById(@PathVariable Long id) {
        Optional<PoolTable> poolTable = poolTableService.getPoolTableById(id);
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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PoolTable>> updatePoolTable(@PathVariable Long id, @RequestBody PoolTable updatedPoolTable) {
        try {
            PoolTable poolTable = poolTableService.updatePoolTable(id, updatedPoolTable);
            ApiResponse<PoolTable> success = ResponseUtils.success(poolTable);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<PoolTable> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // 删除桌台
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePoolTable(@PathVariable Long id) {
        try {
            poolTableService.deletePoolTable(id);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }
}
