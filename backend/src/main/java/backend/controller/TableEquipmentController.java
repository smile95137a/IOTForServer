package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.poolTable.TableEquipment;
import backend.service.TableEquipmentService;
import backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/table-equipments")
public class TableEquipmentController {

    @Autowired
    private TableEquipmentService tableEquipmentService;

    // 获取所有桌台设备
    @GetMapping
    public ResponseEntity<ApiResponse<List<TableEquipment>>> getAllTableEquipments() {
        List<TableEquipment> tableEquipments = tableEquipmentService.getAllTableEquipments();
        ApiResponse<List<TableEquipment>> success = ResponseUtils.success(tableEquipments);
        return ResponseEntity.ok(success);
    }

    // 根据 ID 获取桌台设备
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<TableEquipment>> getTableEquipmentById(@PathVariable String uid) {
        TableEquipment tableEquipment = tableEquipmentService.getTableEquipmentById(uid).orElse(null);
        if (tableEquipment == null) {
            ApiResponse<TableEquipment> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<TableEquipment> success = ResponseUtils.success(tableEquipment);
        return ResponseEntity.ok(success);
    }

    // 创建新的桌台设备
    @PostMapping
    public ResponseEntity<ApiResponse<TableEquipment>> createTableEquipment(@RequestBody TableEquipment tableEquipment) {
        TableEquipment createdTableEquipment = tableEquipmentService.createTableEquipment(tableEquipment);
        ApiResponse<TableEquipment> success = ResponseUtils.success(createdTableEquipment);
        return ResponseEntity.ok(success);
    }

    // 更新桌台设备
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<TableEquipment>> updateTableEquipment(@PathVariable String uid,
                                                                            @RequestBody TableEquipment tableEquipmentDetails) {
        TableEquipment updatedTableEquipment = tableEquipmentService.updateTableEquipment(uid, tableEquipmentDetails);
        if (updatedTableEquipment != null) {
            ApiResponse<TableEquipment> success = ResponseUtils.success(updatedTableEquipment);
            return ResponseEntity.ok(success);
        } else {
            ApiResponse<TableEquipment> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // 删除桌台设备
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deleteTableEquipment(@PathVariable String uid) {
        try {
            tableEquipmentService.deleteTableEquipment(uid);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }
}
