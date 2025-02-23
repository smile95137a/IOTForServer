package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.req.poolTable.EqReq;
import com.frontend.service.EquipmentService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    // ✅ 获取所有桌台设备
    @GetMapping("/table")
    public ResponseEntity<ApiResponse<List<TableEquipment>>> getAllTableEquipments() {
        List<TableEquipment> tableEquipments = equipmentService.getAllTableEquipments();
        return ResponseEntity.ok(ResponseUtils.success(tableEquipments));
    }

    // ✅ 获取所有店家设备
    @GetMapping("/store")
    public ResponseEntity<ApiResponse<List<StoreEquipment>>> getAllStoreEquipments() {
        List<StoreEquipment> storeEquipments = equipmentService.getAllStoreEquipments();
        return ResponseEntity.ok(ResponseUtils.success(storeEquipments));
    }
    
    @GetMapping("/store/{storeId}")
    public ResponseEntity<ApiResponse<List<StoreEquipment>>> getStoreEquipmentsByStoreId(@PathVariable Long storeId) {
        List<StoreEquipment> storeEquipments = equipmentService.findStoreEquipmentsByStoreId(storeId);
        return ResponseEntity.ok(ResponseUtils.success(storeEquipments));
    }





    // ✅ 创建/更新桌台设备
    @PostMapping("/table")
    public ResponseEntity<ApiResponse<TableEquipment>> createTableEquipment(@RequestBody EqReq poolTableEqReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        TableEquipment createdEquipment = equipmentService.saveTableEquipment(poolTableEqReq , id);
        return ResponseEntity.ok(ResponseUtils.success(createdEquipment));
    }

    @PutMapping("/table/{id}")
    public ResponseEntity<ApiResponse<TableEquipment>> updateTableEquipment(
            @PathVariable Long id,
            @RequestBody EqReq poolTableEqReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        TableEquipment updatedEquipment = equipmentService.updateTableEquipment(id, poolTableEqReq, userId);
        return ResponseEntity.ok(ResponseUtils.success(updatedEquipment));
    }

    @GetMapping("/table/{tableId}")
    public ResponseEntity<ApiResponse<List<TableEquipment>>> getTableEquipmentsByTableId(@PathVariable Long tableId) {
        List<TableEquipment> equipments = equipmentService.findTableEquipmentsByTableId(tableId);
        return ResponseEntity.ok(ResponseUtils.success(equipments));
    }
    

    // ✅ 创建/更新店家设备
    @PostMapping("/store")
    public ResponseEntity<ApiResponse<StoreEquipment>> createStoreEquipment(@RequestBody EqReq storeEquipment) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        StoreEquipment createdEquipment = equipmentService.saveStoreEquipment(storeEquipment , id);
        return ResponseEntity.ok(ResponseUtils.success(createdEquipment));
    }

    @PutMapping("/store/{id}")
    public ResponseEntity<ApiResponse<StoreEquipment>> updateStoreEquipment(
            @PathVariable Long id,
            @RequestBody EqReq storeEquipmentReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        StoreEquipment updatedEquipment = equipmentService.updateStoreEquipment(id, storeEquipmentReq, userId);
        return ResponseEntity.ok(ResponseUtils.success(updatedEquipment));
    }


    // ✅ 删除桌台设备
    @DeleteMapping("/table/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTableEquipment(@PathVariable Long id) {
        equipmentService.deleteTableEquipment(id);
        return ResponseEntity.ok(ResponseUtils.success(null));
    }

    // ✅ 删除店家设备
    @DeleteMapping("/store/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStoreEquipment(@PathVariable Long id) {
        equipmentService.deleteStoreEquipment(id);
        return ResponseEntity.ok(ResponseUtils.success(null));
    }

    // ✅ 启用/禁用 桌台设备
    @PutMapping("/table/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateTableEquipmentStatus(@PathVariable Long id, @RequestParam Boolean status) {
        equipmentService.updateTableEquipmentStatus(id, status);
        return ResponseEntity.ok(ResponseUtils.success(null));
    }

    // ✅ 启用/禁用 店家设备
    @PutMapping("/store/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateStoreEquipmentStatus(@PathVariable Long id, @RequestParam Boolean status) {
        equipmentService.updateStoreEquipmentStatus(id, status);
        return ResponseEntity.ok(ResponseUtils.success(null));
    }
}
