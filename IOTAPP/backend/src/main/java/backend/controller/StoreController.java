package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.store.Store;
import backend.service.StoreService;
import backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    // Create a new store
    @PostMapping
    public ResponseEntity<ApiResponse<Store>> createStore(@RequestBody Store store) {
        Store createdStore = storeService.createStore(store);
        ApiResponse<Store> success = ResponseUtils.success(createdStore);
        return ResponseEntity.ok(success);
    }

    // Get a store by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> getStoreById(@PathVariable Long id) {
        Store store = storeService.getStoreById(id).orElse(null);
        if (store == null) {
            ApiResponse<Store> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<Store> success = ResponseUtils.success(store);
        return ResponseEntity.ok(success);
    }

    // Get all stores
    @GetMapping
    public ResponseEntity<ApiResponse<List<Store>>> getAllStores() {
        List<Store> stores = storeService.getAllStores();
        ApiResponse<List<Store>> success = ResponseUtils.success(stores);
        return ResponseEntity.ok(success);
    }

    // Update a store
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Store>> updateStore(@PathVariable Long id, @RequestBody Store updatedStore) {
        try {
            Store store = storeService.updateStore(id, updatedStore);
            ApiResponse<Store> success = ResponseUtils.success(store);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Store> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // Delete a store
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long id) {
        try {
            storeService.deleteStore(id);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }
}
