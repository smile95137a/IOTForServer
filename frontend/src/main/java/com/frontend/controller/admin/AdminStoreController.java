package com.frontend.controller.admin;

import java.util.List;

import com.frontend.res.store.AdminStoreRes;
import com.frontend.res.store.StoreRes;
import com.frontend.utils.ImageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.store.Store;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.web.multipart.MultipartFile;


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

    @GetMapping("/{vendorId}/stores")
    public ResponseEntity<ApiResponse<List<StoreRes>>> getStoresByVendorId(@PathVariable Long vendorId) {
        List<StoreRes> stores = storeService.getStoresByVendorId(vendorId);
        if (stores.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(null));
        }
        return ResponseEntity.ok(ResponseUtils.success(stores));
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

    @PostMapping("/{storeId}/upload-image")
    public ResponseEntity<?> uploadImage(@PathVariable Long storeId, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseUtils.error(400, "文件不能為空", null));
            }

            // 上傳所有文件，並獲取文件路徑列表
            String uploadedFilePath = ImageUtil.upload(file);

            // 存儲到數據庫
            storeService.uploadProductImg(storeId, uploadedFilePath);

            ApiResponse<String> response = ResponseUtils.success(200, "文件上傳成功", uploadedFilePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = ResponseUtils.error(500, "文件上傳失敗", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
