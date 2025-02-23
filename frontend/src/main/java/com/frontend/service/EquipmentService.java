package com.frontend.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.repo.StoreEquipmentRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.TableEquipmentRepository;
import com.frontend.req.poolTable.EqReq;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final TableEquipmentRepository tableEquipmentRepository;
    private final StoreEquipmentRepository storeEquipmentRepository;
    private final StoreRepository storeRepository;
    

    // ✅ 获取所有桌台设备
    public List<TableEquipment> getAllTableEquipments() {
        return tableEquipmentRepository.findAll();
    }

    // ✅ 获取所有店家设备
    public List<StoreEquipment> getAllStoreEquipments() {
        return storeEquipmentRepository.findAll();
    }
    
    public List<StoreEquipment> findStoreEquipmentsByStoreId(Long storeId) {
        return storeEquipmentRepository.findByStoreId(storeId);
    }

    public List<TableEquipment> findTableEquipmentsByTableId(Long tableId) {
        return tableEquipmentRepository.findByPoolTableId(tableId);
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
    public TableEquipment saveTableEquipment(EqReq poolTableEqReq , Long id) {
        TableEquipment tableEquipment = new TableEquipment();
        tableEquipment.setUid(UUID.randomUUID().toString());
        tableEquipment.setStatus(false);
        tableEquipment.setAutoStartTime(LocalTime.now());
        tableEquipment.setAutoStopTime(LocalTime.now());
        tableEquipment.setDescription(poolTableEqReq.getDescription());
        tableEquipment.setEquipmentName(poolTableEqReq.getName());
        tableEquipment.setPoolTable(poolTableEqReq.getPoolTable());
        tableEquipment.setCreateTime(LocalDateTime.now());
        tableEquipment.setCreateUserId(id);
        return tableEquipmentRepository.save(tableEquipment);
    }

    // ✅ 创建/更新店家设备
    public StoreEquipment saveStoreEquipment(EqReq storeEquipmentReq, Long id) {
        // 先從資料庫查找 store，確保它存在
        Store store = storeRepository.findById(storeEquipmentReq.getStore().getId())
            .orElseThrow(() -> new RuntimeException("店家不存在"));

        StoreEquipment storeEquipment = new StoreEquipment();
        storeEquipment.setUid(UUID.randomUUID().toString());
        storeEquipment.setStatus(false);
        storeEquipment.setAutoStartTime(LocalTime.now());
        storeEquipment.setAutoStopTime(LocalTime.now());
        storeEquipment.setDescription(storeEquipmentReq.getDescription());
        storeEquipment.setEquipmentName(storeEquipmentReq.getName());
        storeEquipment.setStore(store); 
        storeEquipment.setCreateTime(LocalDateTime.now());
        storeEquipment.setCreateUserId(id);

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

    public TableEquipment updateTableEquipment(Long id, EqReq poolTableEqReq, Long userId) {
        // 查找設備是否存在
        TableEquipment tableEquipment = tableEquipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("設備不存在"));

        // 更新設備信息
        tableEquipment.setEquipmentName(poolTableEqReq.getName());
        tableEquipment.setDescription(poolTableEqReq.getDescription());
        tableEquipment.setAutoStartTime(poolTableEqReq.getAutoStartTime());
        tableEquipment.setAutoStopTime(poolTableEqReq.getAutoStopTime());
        tableEquipment.setUpdateTime(LocalDateTime.now());
        tableEquipment.setUpdateUserId(userId);

        if (poolTableEqReq.getPoolTable() != null) {
            tableEquipment.setPoolTable(poolTableEqReq.getPoolTable());
        }

        return tableEquipmentRepository.save(tableEquipment);
    }

    public StoreEquipment updateStoreEquipment(Long id, EqReq storeEquipmentReq, Long userId) {
        // 查找設備是否存在
        StoreEquipment storeEquipment = storeEquipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("設備不存在"));

        // 更新設備信息
        storeEquipment.setEquipmentName(storeEquipmentReq.getName());
        storeEquipment.setDescription(storeEquipmentReq.getDescription());
        storeEquipment.setAutoStartTime(storeEquipmentReq.getAutoStartTime());
        storeEquipment.setAutoStopTime(storeEquipmentReq.getAutoStopTime());
        storeEquipment.setUpdateTime(LocalDateTime.now());
        storeEquipment.setUpdateUserId(userId);

        // 如果 `store` 不為空，則更新
        if (storeEquipmentReq.getStore() != null) {
            storeEquipment.setStore(storeEquipmentReq.getStore());
        }

        return storeEquipmentRepository.save(storeEquipment);
    }


}
