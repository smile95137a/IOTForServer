package backend.service;

import backend.entity.poolTable.TableEquipment;
import backend.repo.TableEquipmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TableEquipmentService {

    @Autowired
    private TableEquipmentRepository tableEquipmentRepository;

    // 获取所有桌台设备
    public List<TableEquipment> getAllTableEquipments() {
        return tableEquipmentRepository.findAll();
    }

    // 根据 ID 获取桌台设备
    public Optional<TableEquipment> getTableEquipmentById(Long id) {
        return tableEquipmentRepository.findById(id);
    }

    // 创建新的桌台设备
    public TableEquipment createTableEquipment(TableEquipment tableEquipment) {
        return tableEquipmentRepository.save(tableEquipment);
    }

    // 更新桌台设备
    public TableEquipment updateTableEquipment(Long id, TableEquipment tableEquipmentDetails) {
        Optional<TableEquipment> tableEquipmentOptional = tableEquipmentRepository.findById(id);
        if (tableEquipmentOptional.isPresent()) {
            TableEquipment tableEquipment = tableEquipmentOptional.get();
            tableEquipment.setEquipmentName(tableEquipmentDetails.getEquipmentName());
            tableEquipment.setStatus(tableEquipmentDetails.getStatus());
            tableEquipment.setAutoStartTime(tableEquipmentDetails.getAutoStartTime());
            tableEquipment.setAutoStopTime(tableEquipmentDetails.getAutoStopTime());
            tableEquipment.setDescription(tableEquipmentDetails.getDescription());
            tableEquipment.setUpdateTime(tableEquipmentDetails.getUpdateTime());
            tableEquipment.setUpdateUserId(tableEquipmentDetails.getUpdateUserId());
            return tableEquipmentRepository.save(tableEquipment);
        } else {
            return null; // 或抛出一个自定义异常
        }
    }

    // 删除桌台设备
    public void deleteTableEquipment(Long id) {
        tableEquipmentRepository.deleteById(id);
    }
}
