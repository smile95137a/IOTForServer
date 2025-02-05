package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.frontend.entity.store.Store;
import com.frontend.entity.vendor.Vendor;
import com.frontend.res.vendor.VendorRes;
import com.frontend.service.VendorService;
import com.frontend.utils.RandomUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/vendors")
public class VendorController {

    @Autowired
    private VendorService vendorService;

    @GetMapping("/init")
    public ResponseEntity<ApiResponse<List<Vendor>>> initCreateStore() {
        Set<Store> stores = new HashSet<>();

        List<Vendor> vendors = new ArrayList<>(Arrays.asList(
                new Vendor(null, RandomUtils.genRandom(32, false), "廠商1", "0912345678",stores,   LocalDateTime.now(), 1L, null,null),
                new Vendor(null, RandomUtils.genRandom(32, false), "廠商2", "0922345678",stores,   LocalDateTime.now(), 1L, null,null)
        ));
        List<Vendor> vendors1 = vendorService.initCreateVendor(vendors);
        return ResponseEntity.ok(ResponseUtils.success(vendors1));
    }

    // Get a vendor by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<List<VendorRes>>> countAvailablePoolTables(@PathVariable String uid) {
        List<VendorRes> vendorRes = vendorService.countAvailablePoolTables(uid).orElse(Collections.emptyList());
        if (vendorRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, null, null));
        } else {
            return ResponseEntity.ok(ResponseUtils.success(vendorRes));
        }
    }
}
