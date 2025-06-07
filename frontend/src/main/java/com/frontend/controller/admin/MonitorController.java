package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.monitor.Monitor;
import com.frontend.req.monitor.MonitorReq;
import com.frontend.req.monitor.MonitorUpdateReq;
import com.frontend.res.monitor.MonitorRes;
import com.frontend.service.MonitorService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private final MonitorService monitorService;

    // 新增監視器
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Monitor>> createMonitor(@RequestBody MonitorReq monitorReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        Monitor monitor = monitorService.createMonitor(monitorReq, id);
        return ResponseEntity.ok(ResponseUtils.success(monitor));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MonitorRes>>> getAll() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        List<MonitorRes> monitor = monitorService.getAll();
        return ResponseEntity.ok(ResponseUtils.success(monitor));
    }

    // 更新監視器
    @PutMapping("/update")
    public ResponseEntity<ApiResponse<Monitor>> updateMonitor(@RequestBody MonitorUpdateReq monitorUpdateReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        Monitor monitor = monitorService.updateMonitor(monitorUpdateReq, id);
        return ResponseEntity.ok(ResponseUtils.success(monitor));
    }

    // 取得某商店的所有監視器
    @GetMapping("/store/{id}")
    public ResponseEntity<ApiResponse<List<MonitorRes>>> getMonitorsByStoreId(@PathVariable Long id) {
        List<MonitorRes> monitors = monitorService.getMonitorsByStoreId(id);
        return ResponseEntity.ok(ResponseUtils.success(monitors));
    }

    // 刪除監視器
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteMonitor(@PathVariable Long id) {
        monitorService.deleteMonitor(id);
        return ResponseEntity.ok(ResponseUtils.success("Monitor deleted successfully"));
    }
}

