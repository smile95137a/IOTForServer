package backend.service;

import backend.entity.poolTable.PoolTable;
import backend.repo.PoolTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;

    // Create a new pool table
    public List<PoolTable> createPoolTables(List<PoolTable> poolTables) {
        // 使用 saveAll 方法批量保存所有傳入的 PoolTable
        return poolTableRepository.saveAll(poolTables);
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
    public PoolTable updatePoolTable(String uid, PoolTable updatedPoolTable) {
        return poolTableRepository.findByUid(uid).map(poolTable -> {
            poolTable.setTableNumber(updatedPoolTable.getTableNumber());
            poolTable.setStatus(updatedPoolTable.getStatus());
            poolTable.setStore(updatedPoolTable.getStore());
            poolTable.setCreateTime(updatedPoolTable.getCreateTime());
            poolTable.setCreateUserId(updatedPoolTable.getCreateUserId());
            poolTable.setUpdateTime(updatedPoolTable.getUpdateTime());
            poolTable.setUpdateUserId(updatedPoolTable.getUpdateUserId());
            return poolTableRepository.save(poolTable);
        }).orElseThrow(() -> new RuntimeException("PoolTable not found with id: " + uid));
    }

    // Delete a pool table
    public void deletePoolTable(String uid) {
        poolTableRepository.deleteByUid(uid);
    }
}
