package backend.controller;

import backend.config.message.ApiResponse;
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
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> getVendorById(@PathVariable Long id) {
        Optional<Vendor> vendor = vendorService.getVendorById(id);
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
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Vendor>> updateVendor(@PathVariable Long id, @RequestBody Vendor updatedVendor) {
        try {
            Vendor vendor = vendorService.updateVendor(id, updatedVendor);
            return ResponseEntity.ok(ResponseUtils.success(vendor));
        } catch (RuntimeException e) {
            // Log the error and provide specific error message
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    // Delete a vendor
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteVendor(@PathVariable Long id) {
        try {
            vendorService.deleteVendor(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            // Log the error and provide specific error message
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }
}
