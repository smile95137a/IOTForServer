package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.repo.GameRecordRepository;
import com.frontend.repo.TableEquipmentRepository;
import com.frontend.res.poolTable.AdminPoolTableRes;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.repo.PoolTableRepository;
import com.frontend.utils.RandomUtils;

@Service
public class AdminPoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;
    @Autowired
    private TableEquipmentRepository tableEquipmentRepository;
    @Autowired
    private GameRecordRepository gameRecordRepository;

    // Create a new pool table
    public PoolTable createPoolTable(PoolTableReq poolTableReq, Long id) {
        PoolTable poolTable = convertToEntity(poolTableReq);
        poolTable.setUid(RandomUtils.genRandom(24)); // 生成唯一 UID
        poolTable.setCreateTime(LocalDateTime.now());
        poolTable.setCreateUserId(id);
        return poolTableRepository.save(poolTable);
    }

    private PoolTable convertToEntity(PoolTableReq req) {
        PoolTable poolTable = new PoolTable();
        poolTable.setTableNumber(req.getTableNumber());
        poolTable.setStatus(req.getStatus());
        if(req.getStore() != null){
            poolTable.setStore(req.getStore());
        }
        // 這裡假設 Store 是直接從 PoolTableReq 傳過來的
//        if(req.getTableEquipments() != null){
//            poolTable.setTableEquipments(req.getTableEquipments());
//        }
        poolTable.setIsUse(false);

        return poolTable;
    }


    public Optional<AdminPoolTableRes> getPoolTableById(String uid) {
        Optional<PoolTable> poolTable = poolTableRepository.findByUid(uid);
        if (poolTable.isPresent()) {
            AdminPoolTableRes adminPoolTableRes = convertToAdminPoolTableRes(poolTable.get());
            return Optional.of(adminPoolTableRes);
        }
        return Optional.empty();
    }

    public List<AdminPoolTableRes> getAllPoolTables() {
        List<PoolTable> poolTables = poolTableRepository.findAll();
        return poolTables.stream()
                .map(this::convertToAdminPoolTableRes)
                .collect(Collectors.toList());
    }

    private AdminPoolTableRes convertToAdminPoolTableRes(PoolTable poolTable) {
        AdminPoolTableRes.AdminPoolTableResBuilder builder = AdminPoolTableRes.builder()
                .storeId(poolTable.getStore().getId())
                .uid(poolTable.getUid())
                .tableNumber(poolTable.getTableNumber())
                .status(poolTable.getStatus());

        // 只在 tableEquipments 不为 null 时设置 tableEquipments
//        if (poolTable.getTableEquipments() != null) {
//            builder.tableEquipments(poolTable.getTableEquipments());
//        }

        return builder.build();
    }



    // Update a pool table
    public PoolTable updatePoolTable(String uid, PoolTableReq updatedPoolTableReq, Long id) {
        // 根據 UID 查找原有的 PoolTable
        return poolTableRepository.findByUid(uid).map(poolTable -> {
            // 使用更新的 PoolTableReq 來設置新的數據
            poolTable.setTableNumber(updatedPoolTableReq.getTableNumber());
            poolTable.setStatus(updatedPoolTableReq.getStatus());
            poolTable.setIsUse(updatedPoolTableReq.getIsUse());
            if(updatedPoolTableReq.getStore() != null){
                poolTable.setStore(updatedPoolTableReq.getStore());
            }
            // 這裡假設 Store 是直接從 PoolTableReq 傳過來的
//            if(updatedPoolTableReq.getTableEquipments() != null){
//                poolTable.setTableEquipments(updatedPoolTableReq.getTableEquipments());
//            }
            // 設置時間和用戶信息
            poolTable.setUpdateTime(LocalDateTime.now());
            poolTable.setUpdateUserId(id);

            // 保存更新後的 PoolTable
            return poolTableRepository.save(poolTable);
        }).orElseThrow(() -> new RuntimeException("PoolTable not found with uid: " + uid));
    }


    // Delete a pool table
    @Transactional
    public void deletePoolTable(String uid) {
        poolTableRepository.deleteByUid(uid);
    }
    
    public List<PoolTable> findByStoreId(Long storeId) {
        List<PoolTable> poolTables = poolTableRepository.findByStoreId(storeId);

        return poolTables;
    }

    public PoolTable closePoolTable(String uid, PoolTableReq poolTableReq, Long id) {
        PoolTable poolTable = poolTableRepository.findByUid(uid).get();
        poolTable.setIsUse(false);
        for (TableEquipment tableEquipment : poolTableReq.getTableEquipments()) {
            tableEquipment.setStatus(false);
            tableEquipmentRepository.save(tableEquipment);
        }
        poolTable.setUpdateTime(LocalDateTime.now());
        poolTable.setUpdateUserId(id);
        poolTableRepository.save(poolTable);

        GameRecord started = gameRecordRepository.findByPoolTableIdAndStatus(poolTable.getId(), "STARTED");
        if(started != null){
            gameRecordRepository.delete(started);
        }
        return poolTable;
    }
}
