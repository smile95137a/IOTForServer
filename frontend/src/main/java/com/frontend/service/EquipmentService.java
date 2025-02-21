package com.frontend.service;

import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.repo.StoreEquipmentRepository;
import com.frontend.repo.TableEquipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final TableEquipmentRepository tableEquipmentRepository;
    private final StoreEquipmentRepository storeEquipmentRepository;

    // ✅ 获取所有桌台设备
    public List<TableEquipment> getAllTableEquipments() {
        return tableEquipmentRepository.findAll();
    }

    // ✅ 获取所有店家设备
    public List<StoreEquipment> getAllStoreEquipments() {
        return storeEquipmentRepository.findAll();
    }

    // ✅ 通过 ID 获取桌台设备
    public Optional<TableEquipment> getTableEquipmentById(Long id) {
        return tableEquipmentRepository.findById(id);
    }

    // ✅ 通过 ID 获取店家设备
    public Optional<StoreEquipment> getStoreEquipmentById(Long id) {
        return storeEquipmentRepository.findById(id);
    }

    // ✅ 创建/更新桌台设备
    public TableEquipment saveTableEquipment(TableEquipment tableEquipment) {
        return tableEquipmentRepository.save(tableEquipment);
    }

    // ✅ 创建/更新店家设备
    public StoreEquipment saveStoreEquipment(StoreEquipment storeEquipment) {
        return storeEquipmentRepository.save(storeEquipment);
    }

    // ✅ 删除桌台设备
    public void deleteTableEquipment(Long id) {
        tableEquipmentRepository.deleteById(id);
    }

    // ✅ 删除店家设备
    public void deleteStoreEquipment(Long id) {
        storeEquipmentRepository.deleteById(id);
    }

    // ✅ 启用/禁用 桌台设备
    public void updateTableEquipmentStatus(Long id, Boolean status) {
        tableEquipmentRepository.findById(id).ifPresent(equipment -> {
            equipment.setStatus(status);
            tableEquipmentRepository.save(equipment);
        });
    }

    // ✅ 启用/禁用 店家设备
    public void updateStoreEquipmentStatus(Long id, Boolean status) {
        storeEquipmentRepository.findById(id).ifPresent(equipment -> {
            equipment.setStatus(status);
            storeEquipmentRepository.save(equipment);
        });
    }
}
