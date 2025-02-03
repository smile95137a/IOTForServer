package src.main.java.com.frontend.controller;

import src.main.java.com.common.config.message.ApiResponse;
import src.main.java.com.common.config.message.ResponseUtils;
import src.main.java.com.frontend.entity.poolTable.PoolTable;
import src.main.java.com.frontend.entity.store.Store;
import src.main.java.com.frontend.entity.vendor.Vendor;
import src.main.java.com.frontend.res.store.StoreRes;
import src.main.java.com.frontend.service.StoreService;
import src.main.java.com.frontend.utils.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/test/stores")
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("/init/{venderId}")
    public ResponseEntity<ApiResponse<List<Store>>> initCreateStore(@PathVariable Long venderId) {
        Vendor vendor = new Vendor();
        vendor.setId(venderId);
        Set<PoolTable> poolTables = new HashSet<>();
        Long userId = 1L;
        List<Store> stores = new ArrayList<>(Arrays.asList(
                new Store(null, RandomUtils.genRandom(32, false), "板橋旗艦店", "新北市板橋區文化路一段280號B1", vendor, poolTables,null, LocalDateTime.now(), userId, null, null),
                new Store(null, RandomUtils.genRandom(32, false), "台北旗艦店", "台北市中正區寶慶路一段1號2F", vendor,poolTables, null, LocalDateTime.now(), userId, null, null)
        ));
        List<Store> stores1 = storeService.initCreateStore(stores);
        ApiResponse<List<Store>> success = ResponseUtils.success(stores1);
        return ResponseEntity.ok(success);
    }

    // Get a store by ID
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<List<StoreRes>>> countAvailableAndInUseByUid(@PathVariable String uid) {
        List<StoreRes> storeRes = storeService.countAvailableAndInUseByUid(uid).orElse(Collections.emptyList());
        if (storeRes.isEmpty()) {
            return ResponseEntity.ok(ResponseUtils.error(9999, null, null));
        }
        return ResponseEntity.ok(ResponseUtils.success(storeRes));
    }

}
