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
    public PoolTable createPoolTable(PoolTable poolTable) {
        return poolTableRepository.save(poolTable);
    }

    // Retrieve a pool table by ID
    public Optional<PoolTable> getPoolTableById(Long id) {
        return poolTableRepository.findById(id);
    }

    // Retrieve all pool tables
    public List<PoolTable> getAllPoolTables() {
        return poolTableRepository.findAll();
    }

    // Update a pool table
    public PoolTable updatePoolTable(Long id, PoolTable updatedPoolTable) {
        return poolTableRepository.findById(id).map(poolTable -> {
            poolTable.setTableNumber(updatedPoolTable.getTableNumber());
            poolTable.setStatus(updatedPoolTable.getStatus());
            poolTable.setStore(updatedPoolTable.getStore());
            poolTable.setCreateTime(updatedPoolTable.getCreateTime());
            poolTable.setCreateUserId(updatedPoolTable.getCreateUserId());
            poolTable.setUpdateTime(updatedPoolTable.getUpdateTime());
            poolTable.setUpdateUserId(updatedPoolTable.getUpdateUserId());
            return poolTableRepository.save(poolTable);
        }).orElseThrow(() -> new RuntimeException("PoolTable not found with id: " + id));
    }

    // Delete a pool table
    public void deletePoolTable(Long id) {
        poolTableRepository.deleteById(id);
    }
}
