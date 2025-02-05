package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.store.Store;
import backend.entity.vendor.Vendor;
import backend.service.VendorService;
import backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    // Create a new vendor
    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody Vendor vendor) {
        try {
            Vendor createdVendor = vendorService.createVendor(vendor);
            ApiResponse<Vendor> response = ResponseUtils.success(createdVendor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Log the error (could use a logger here)
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    // Get a vendor by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable String uid) {
        Optional<Vendor> vendor = vendorService.getVendorById(uid);
        if (vendor.isPresent()) {
            return ResponseEntity.ok(ResponseUtils.success(vendor.get()));
        } else {
            return ResponseEntity.ok(ResponseUtils.error(9999, null, null));
        }
    }

    // Get all vendors
    @GetMapping
    public ResponseEntity<ApiResponse<List<Vendor>>> getAllVendors() {
        List<Vendor> vendors = vendorService.getAllVendors();
        return ResponseEntity.ok(ResponseUtils.success(vendors));
    }

    // Update a vendor
    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable String uid, @RequestBody Vendor updatedVendor) {
        try {
            Vendor vendor = vendorService.updateVendor(uid, updatedVendor);
            return ResponseEntity.ok(ResponseUtils.success(vendor));
        } catch (RuntimeException e) {
            // Log the error and provide specific error message
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    // Delete a vendor
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable String uid) {
        try {
            vendorService.deleteVendor(uid);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Log the error and provide specific error message
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @PostMapping("/{vendorId}/stores")
    public ResponseEntity<ApiResponse<Store>> addStoreToVendor(@PathVariable Long vendorId, @RequestBody Store store) {
        try {
            // 根據廠商ID查詢廠商
            Vendor vendor = vendorService.getVendorById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

            // 設定商店與廠商的關聯
            store.setVendor(vendor);

            // 儲存商店
            Store savedStore = vendorService.addStoreToVendor(vendor, store);

            // 返回成功的響應
            ApiResponse<Store> response = ResponseUtils.success(savedStore);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 出現錯誤時返回錯誤響應
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @GetMapping("/{vendorId}/stores")
    public ResponseEntity<ApiResponse<List<Store>>> getStoresByVendor(@PathVariable Long vendorId) {
        try {
            // 根據廠商ID查詢廠商
            Vendor vendor = vendorService.getVendorById(vendorId)
                    .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

            // 查詢該廠商的所有商店
            List<Store> stores = vendorService.getStoresByVendor(vendor);

            // 返回成功的響應
            ApiResponse<List<Store>> response = ResponseUtils.success(stores);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 出現錯誤時返回錯誤響應
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
    @PutMapping("/{storeId}/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<Store>> updateStoreVendor(@PathVariable Long storeId, @PathVariable Long vendorId) {
        try {
            // 更新商店的廠商關聯
            Store updatedStore = vendorService.updateStoreVendor(storeId, vendorId);
            return ResponseEntity.ok(ResponseUtils.success(updatedStore));
        } catch (Exception e) {
            // 出現錯誤時返回錯誤響應
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
    @DeleteMapping("/{storeId}")
    public ResponseEntity<ApiResponse<Void>> deleteStore(@PathVariable Long storeId) {
        try {
            vendorService.deleteStore(storeId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

}
