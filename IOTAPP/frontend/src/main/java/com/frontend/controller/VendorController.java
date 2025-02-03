package src.main.java.com.frontend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import src.main.java.com.common.config.message.ApiResponse;
import src.main.java.com.common.config.message.ResponseUtils;
import src.main.java.com.frontend.entity.store.Store;
import src.main.java.com.frontend.entity.vendor.Vendor;
import src.main.java.com.frontend.res.vendor.VendorRes;
import src.main.java.com.frontend.service.VendorService;
import src.main.java.com.frontend.utils.RandomUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/test/vendors")
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
