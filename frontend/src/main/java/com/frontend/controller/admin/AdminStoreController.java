package com.frontend.controller.admin;

import java.util.List;

import com.frontend.res.store.AdminStoreRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.store.Store;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;


@RestController
@RequestMapping("/api/b/stores")
public class AdminStoreController {

    @Autowired
    private AdminStoreService storeService;

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
    public ResponseEntity<ApiResponse<AdminStoreRes>> getStoreById(@PathVariable String uid) {
        AdminStoreRes store = storeService.getStoreById(uid).orElse(null);
        if (store == null) {
            ApiResponse<AdminStoreRes> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<AdminStoreRes> success = ResponseUtils.success(store);
        return ResponseEntity.ok(success);
    }

    // Get all stores
    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminStoreRes>>> getAllStores() {
        List<AdminStoreRes> stores = storeService.getAllStores();
        ApiResponse<List<AdminStoreRes>> success = ResponseUtils.success(stores);
        return ResponseEntity.ok(success);
    }

    // Update a store
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<Store>> updateStore(@PathVariable String uid, @RequestBody StoreReq store) {
        try {
//            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
//            Long id = securityUser.getId();
            Store storeObj = storeService.updateStore(uid, store , 4L);
            ApiResponse<Store> success = ResponseUtils.success(storeObj);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            e.printStackTrace();
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
