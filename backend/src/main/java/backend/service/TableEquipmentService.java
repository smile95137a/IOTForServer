package src.main.java.backend.service;

import src.main.java.backend.entity.poolTable.TableEquipment;
import src.main.java.backend.repo.TableEquipmentRepository;
import src.main.java.backend.utils.RandomUtils;
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
    public Optional<TableEquipment> getTableEquipmentById(String uid) {
        return tableEquipmentRepository.findByUid(uid);
    }

    // 创建新的桌台设备
    public TableEquipment createTableEquipment(TableEquipment tableEquipment) {
        String s = RandomUtils.genRandom(24);
        tableEquipment.setUid(s);
        return tableEquipmentRepository.save(tableEquipment);
    }

    // 更新桌台设备
    public TableEquipment updateTableEquipment(String uid, TableEquipment tableEquipmentDetails) {
        Optional<TableEquipment> tableEquipmentOptional = tableEquipmentRepository.findByUid(uid);
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
    public void deleteTableEquipment(String uid) {
        tableEquipmentRepository.deleteByUid(uid);
    }
}
