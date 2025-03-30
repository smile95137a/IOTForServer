package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.router.Router;
import com.frontend.enums.RouterType;
import com.frontend.req.router.AddRouterRequest;
import com.frontend.req.router.UpdateRouterRequest;
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

    // 新增 Router
    @PostMapping
    public ResponseEntity<ApiResponse<String>> addRouter(@RequestBody AddRouterRequest request) {
        Router router = routerService.addRouter(request.getStoreId(), request.getRouterType(), request.getNumber());
        int routerCount = routerService.getRouterCountByStoreId(request.getStoreId());
        String message = "Router created with ID: " + router.getId() +
                ", It's the " + routerCount + "th router in this store.";
        return ResponseEntity.ok(ResponseUtils.success(message));
    }

    // 更新 Router
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Router>> updateRouter(
            @PathVariable Long id,
            @RequestBody UpdateRouterRequest request) {
        Router router = routerService.updateRouter(id, request.getRouterType(), request.getNumber());
        return ResponseEntity.ok(ResponseUtils.success(router));
    }

    // 4. 移除 Router
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<?>> deleteRouter(@PathVariable Long id) {
        routerService.deleteRouter(id);
        return ResponseEntity.ok(ResponseUtils.success(true));
    }

    // 取得 Router 種類
    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getRouterTypes() {
        List<Map<String, String>> routerTypes = Arrays.stream(RouterType.values())
                .map(type -> Map.of(
                        "value", type.name(), // 英文名稱
                        "label", type.getDisplayName() // 中文名稱
                ))
                .toList();
        return ResponseEntity.ok(ResponseUtils.success(routerTypes));
    }
}
