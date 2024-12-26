package backend.controller;

import backend.entity.poolTable.TableEquipment;
import backend.service.TableEquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/table-equipments")
public class TableEquipmentController {

    @Autowired
    private TableEquipmentService tableEquipmentService;

    // 获取所有桌台设备
    @GetMapping
    public List<TableEquipment> getAllTableEquipments() {
        return tableEquipmentService.getAllTableEquipments();
    }

    // 根据 ID 获取桌台设备
    @GetMapping("/{id}")
    public ResponseEntity<TableEquipment> getTableEquipmentById(@PathVariable Long id) {
        Optional<TableEquipment> tableEquipment = tableEquipmentService.getTableEquipmentById(id);
        return tableEquipment.map(ResponseEntity::ok)
                             .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 创建新的桌台设备
    @PostMapping
    public ResponseEntity<TableEquipment> createTableEquipment(@RequestBody TableEquipment tableEquipment) {
        TableEquipment createdTableEquipment = tableEquipmentService.createTableEquipment(tableEquipment);
        return new ResponseEntity<>(createdTableEquipment, HttpStatus.CREATED);
    }

    // 更新桌台设备
    @PutMapping("/{id}")
    public ResponseEntity<TableEquipment> updateTableEquipment(@PathVariable Long id,
                                                                @RequestBody TableEquipment tableEquipmentDetails) {
        TableEquipment updatedTableEquipment = tableEquipmentService.updateTableEquipment(id, tableEquipmentDetails);
        if (updatedTableEquipment != null) {
            return ResponseEntity.ok(updatedTableEquipment);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 删除桌台设备
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTableEquipment(@PathVariable Long id) {
        tableEquipmentService.deleteTableEquipment(id);
        return ResponseEntity.noContent().build();
    }
}
