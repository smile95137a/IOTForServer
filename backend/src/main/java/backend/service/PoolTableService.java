package backend.service;

import backend.entity.poolTable.PoolTable;
import backend.repo.PoolTableRepository;
import backend.req.poolTable.PoolTableReq;
import backend.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;

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
        if(req.getTableEquipments() != null){
            poolTable.setTableEquipments(req.getTableEquipments());
        }
        poolTable.setIsUse(false);

        return poolTable;
    }


    // Retrieve a pool table by ID
    public Optional<PoolTable> getPoolTableById(String uid) {
        return poolTableRepository.findByUid(uid);
    }

    // Retrieve all pool tables
    public List<PoolTable> getAllPoolTables() {
        return poolTableRepository.findAll();
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
            if(updatedPoolTableReq.getTableEquipments() != null){
                poolTable.setTableEquipments(updatedPoolTableReq.getTableEquipments());
            }
            // 設置時間和用戶信息
            poolTable.setUpdateTime(LocalDateTime.now());
            poolTable.setUpdateUserId(id);

            // 保存更新後的 PoolTable
            return poolTableRepository.save(poolTable);
        }).orElseThrow(() -> new RuntimeException("PoolTable not found with uid: " + uid));
    }


    // Delete a pool table
    public void deletePoolTable(String uid) {
        poolTableRepository.deleteByUid(uid);
    }
}
