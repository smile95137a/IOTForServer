package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.router.Router;
import com.frontend.enums.RouterType;
import com.frontend.req.router.AddRouterRequest;
import com.frontend.req.router.CircuitControlRequest;
import com.frontend.req.router.RouterCircuitRequest;
import com.frontend.res.poolTable.RouterWithTableInfoResponse;
import com.frontend.res.router.RouterResponse;
import com.frontend.service.RouterService;
import com.frontend.utils.ResponseUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/b/router")
public class AdminRouterController {

    private final RouterService routerService;

    public AdminRouterController(RouterService routerService) {
        this.routerService = routerService;
    }

    // 1. 查詢某個店家的所有 Router
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<RouterResponse>>> getRoutersByStoreId(@PathVariable Long storeId) {
        List<RouterResponse> routers = routerService.getRoutersByStoreId(storeId);
        return ResponseEntity.ok(ResponseUtils.success(routers));
    }

    @GetMapping("/store/{storeId}/{poolTableId}")
    public ResponseEntity<ApiResponse<List<RouterWithTableInfoResponse>>> getRoutersWithTableInfo(
            @PathVariable Long storeId,
            @PathVariable Long poolTableId) {
        List<RouterWithTableInfoResponse> routers = routerService.getRoutersWithTableInfo(storeId, poolTableId);
        return ResponseEntity.ok(ResponseUtils.success(routers));
    }

    // 2. 新增 Router
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addRouter(@RequestBody AddRouterRequest request) {
        Router router = routerService.addRouter(request.getStoreId(), request);
        int routerCount = routerService.getRouterCountByStoreId(request.getStoreId());
        String message = "Router created with ID: " + router.getId() +
                ", It's the " + routerCount + "th router in this store.";
        return ResponseEntity.ok(ResponseUtils.success(message));
    }

    // 3. 更新 Router（更新控制設定）
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Router>> updateRouter(@PathVariable Long id,
                                                            @RequestBody RouterCircuitRequest request) {
        Router updated = routerService.updateRouter(id, request);
        return ResponseEntity.ok(ResponseUtils.success(updated));
    }

    // 4. 移除 Router
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRouter(@PathVariable Long id) {
        routerService.deleteRouter(id);
        return ResponseEntity.ok(ResponseUtils.success(true));
    }

    // 5. 取得 Router 類型（前端選單用）
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRouterTypes() {
        List<Map<String, String>> routerTypes = Arrays.stream(RouterType.values())
                .map(type -> Map.of(
                        "value", type.name(),
                        "label", type.getDisplayName()
                ))
                .toList();
        return ResponseEntity.ok(ResponseUtils.success(routerTypes));
    }

    // 6. 控制單一 Router 開關（Modbus DO 寫入）
    @PostMapping("/control")
    public ResponseEntity<ApiResponse<Boolean>> controlRouter(@RequestBody CircuitControlRequest request) throws Exception {
        boolean result = routerService.controlCircuit(request);
        return ResponseEntity.ok(ResponseUtils.success(result));
    }
}
