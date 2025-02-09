package com.frontend.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.store.Store;
import com.frontend.entity.vendor.Vendor;
import com.frontend.service.VendorService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;

@RestController
@RequestMapping("/api/b/vendors")
public class AdminVendorController {

    @Autowired
    private VendorService vendorService;

    // Create a new vendor
    @PostMapping
    public ResponseEntity<ApiResponse<Vendor>> createVendor(@RequestBody VendorReq vendor) {
        try {
//            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
//            Long id = securityUser.getId();
            Vendor createdVendor = vendorService.createVendor(vendor, 4L);
            ApiResponse<Vendor> response = ResponseUtils.success(createdVendor);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    // Get a vendor by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable String uid) {
        Optional<Vendor> vendor = vendorService.getVendorByUid(uid);
        if (vendor.isPresent()) {
            return ResponseEntity.ok(ResponseUtils.success(vendor.get()));
        } else {
            return ResponseEntity.ok(ResponseUtils.error(9999, "Vendor not found", null));
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
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable String uid, @RequestBody VendorReq vendor) {
        try {
//            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
//            Long id = securityUser.getId();
            Vendor updatedVendor = vendorService.updateVendor(uid, vendor, 4L);
            return ResponseEntity.ok(ResponseUtils.success(updatedVendor));
        } catch (RuntimeException e) {
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
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }


    @PutMapping("/{storeId}/vendor/{vendorId}")
    public ResponseEntity<ApiResponse<Store>> updateStoreVendor(@PathVariable Long storeId, @PathVariable Long vendorId) {
        try {
            Store updatedStore = vendorService.updateStoreVendor(storeId, vendorId);
            return ResponseEntity.ok(ResponseUtils.success(updatedStore));
        } catch (Exception e) {
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
