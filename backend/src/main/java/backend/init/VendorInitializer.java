package src.main.java.backend.init;

import src.main.java.backend.entity.poolTable.PoolTable;
import src.main.java.backend.entity.store.Store;
import src.main.java.backend.entity.vendor.Vendor;
import src.main.java.backend.repo.PoolTableRepository;
import src.main.java.backend.repo.StoreRepository;
import src.main.java.backend.repo.VendorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(4)
public class VendorInitializer implements CommandLineRunner {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Override
    public void run(String... args) throws Exception {

        if (vendorRepository.count() > 0) {
            return; // 若已有廠商資料，則跳過創建
        }
        // 1. 創建廠商
        Vendor vendorA = createVendor("Vendor A", "contact@vendora.com");

        // 2. 創建店家，並關聯到廠商
        Store store1 = createStore(vendorA, "Store 1", "123 Main St", "store-001");
        Store store2 = createStore(vendorA, "Store 2", "456 Second Ave", "store-002");

        // 3. 創建桌台，並關聯到店家
        createPoolTables(store1, "AVAILABLE", 6);
        createPoolTables(store2, "AVAILABLE", 6);
    }

    // 1. 創建廠商
    private Vendor createVendor(String name, String contactInfo) {
        Vendor vendor = new Vendor();
        vendor.setName(name);
        vendor.setContactInfo(contactInfo);
        vendor.setCreateTime(LocalDateTime.now());
        vendor.setCreateUserId(1L);  // 假設是由用戶ID 1創建
        vendor.setUpdateTime(LocalDateTime.now());
        vendor.setUpdateUserId(1L);

        return vendorRepository.save(vendor);
    }

    // 2. 創建店家並關聯到廠商
    private Store createStore(Vendor vendor, String storeName, String address, String storeUid) {
        Store store = new Store();
        store.setVendor(vendor);
        store.setName(storeName);
        store.setAddress(address);
        store.setUid(storeUid);
        store.setCreateTime(LocalDateTime.now());
        store.setCreateUserId(1L);  // 假設是由用戶ID 1創建
        store.setUpdateTime(LocalDateTime.now());
        store.setUpdateUserId(1L);

        // 儲存店家後返回store，與廠商關聯
        return storeRepository.save(store);
    }

    // 3. 創建桌台並關聯到店家
    private void createPoolTables(Store store, String status, int numTables) {
        Set<PoolTable> poolTables = new HashSet<>();

        // 創建桌台
        for (int i = 1; i <= numTables; i++) {
            PoolTable poolTable = new PoolTable();
            poolTable.setStore(store);
            poolTable.setTableNumber("table-" + i);
            poolTable.setStatus(status);
            poolTable.setCreateTime(LocalDateTime.now());
            poolTable.setCreateUserId(1L);  // 假設是由用戶ID 1創建
            poolTable.setUpdateTime(LocalDateTime.now());
            poolTable.setUpdateUserId(1L);

            poolTables.add(poolTable);
        }

        // 保存桌台資料
        poolTableRepository.saveAll(poolTables);

        // 保存店家資料及其關聯的桌台
        store.setPoolTables(poolTables);  // 關聯桌台到店家
        storeRepository.save(store); // 更新店家資料，保存桌台關聯
    }

}
