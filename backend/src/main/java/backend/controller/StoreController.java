package backend.controller;

import backend.config.message.ApiResponse;
import backend.config.service.UserPrinciple;
import backend.entity.store.Store;
import backend.req.store.StoreReq;
import backend.service.StoreService;
import backend.utils.ResponseUtils;
import backend.utils.SecurityUtils;
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
    public ResponseEntity<ApiResponse<Store>> createStore(@RequestBody StoreReq store) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        Store createdStore = storeService.createStore(store , id);
        ApiResponse<Store> success = ResponseUtils.success(createdStore);
        return ResponseEntity.ok(success);
    }

    // Get a store by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<Store>> getStoreById(@PathVariable String uid) {
        Store store = storeService.getStoreById(uid).orElse(null);
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
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<Store>> updateStore(@PathVariable String uid, @RequestBody StoreReq store) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            Store storeObj = storeService.updateStore(uid, store , id);
            ApiResponse<Store> success = ResponseUtils.success(storeObj);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Store> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // Delete a store
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable String uid) {
        try {
            storeService.deleteStore(uid);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

}
