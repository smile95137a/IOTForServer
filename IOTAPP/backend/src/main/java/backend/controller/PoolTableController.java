package backend.controller;

import backend.entity.poolTable.PoolTable;
import backend.service.PoolTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/poolTables")
public class PoolTableController {

    @Autowired
    private PoolTableService poolTableService;

    // Create a new pool table
    @PostMapping
    public ResponseEntity<PoolTable> createPoolTable(@RequestBody PoolTable poolTable) {
        PoolTable createdPoolTable = poolTableService.createPoolTable(poolTable);
        return ResponseEntity.ok(createdPoolTable);
    }

    // Get a pool table by ID
    @GetMapping("/{id}")
    public ResponseEntity<PoolTable> getPoolTableById(@PathVariable Long id) {
        Optional<PoolTable> poolTable = poolTableService.getPoolTableById(id);
        return poolTable.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    // Get all pool tables
    @GetMapping
    public ResponseEntity<List<PoolTable>> getAllPoolTables() {
        List<PoolTable> poolTables = poolTableService.getAllPoolTables();
        return ResponseEntity.ok(poolTables);
    }

    // Update a pool table
    @PutMapping("/{id}")
    public ResponseEntity<PoolTable> updatePoolTable(@PathVariable Long id, @RequestBody PoolTable updatedPoolTable) {
        try {
            PoolTable poolTable = poolTableService.updatePoolTable(id, updatedPoolTable);
            return ResponseEntity.ok(poolTable);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Delete a pool table
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePoolTable(@PathVariable Long id) {
        try {
            poolTableService.deletePoolTable(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
