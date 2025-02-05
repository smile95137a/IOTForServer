package src.main.java.backend.service;

import src.main.java.backend.entity.store.Store;
import src.main.java.backend.entity.vendor.Vendor;
import src.main.java.backend.repo.StoreRepository;
import src.main.java.backend.repo.VendorRepository;
import src.main.java.backend.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VendorService {

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private StoreRepository storeRepository;

    // Create a new vendor
    public Vendor createVendor(Vendor vendor) {

        String s = RandomUtils.genRandom(24);
        vendor.setUid(s);
        return vendorRepository.save(vendor);
    }

    // Retrieve a vendor by ID
    public Optional<Vendor> getVendorById(String uid) {
        return vendorRepository.findByUid(uid);
    }

    // Retrieve all vendors
    public List<Vendor> getAllVendors() {
        return vendorRepository.findAll();
    }

    // Update a vendor
    public Vendor updateVendor(String uid, Vendor updatedVendor) {
        return vendorRepository.findByUid(uid).map(vendor -> {
            vendor.setName(updatedVendor.getName());
            vendor.setContactInfo(updatedVendor.getContactInfo());
            vendor.setStores(updatedVendor.getStores());
            vendor.setCreateTime(updatedVendor.getCreateTime());
            vendor.setCreateUserId(updatedVendor.getCreateUserId());
            vendor.setUpdateTime(updatedVendor.getUpdateTime());
            vendor.setUpdateUserId(updatedVendor.getUpdateUserId());
            return vendorRepository.save(vendor);
        }).orElseThrow(() -> new RuntimeException("Vendor not found with id: " + uid));
    }

    // Delete a vendor
    public void deleteVendor(String uid) {
        vendorRepository.deleteByUid(uid);
    }

    // 創建商店並與廠商關聯
    public Store addStoreToVendor(Vendor vendor, Store store) {
        store.setVendor(vendor);  // 設置商店的廠商關聯
        return storeRepository.save(store);  // 保存商店
    }

    // 根據廠商查詢所有商店
    public List<Store> getStoresByVendor(Vendor vendor) {
        return new ArrayList<>(vendor.getStores());
    }

    // 更新商店的廠商關聯
    public Store updateStoreVendor(Long storeId, Long vendorId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new RuntimeException("Vendor not found with id: " + vendorId));

        store.setVendor(vendor);
        return storeRepository.save(store);
    }

    // 刪除商店並解除廠商關聯
    public void deleteStore(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new RuntimeException("Store not found with id: " + storeId));

        store.setVendor(null);  // 解除廠商與商店的關聯
        storeRepository.delete(store);  // 刪除商店
    }

    // 根據ID查詢廠商
    public Optional<Vendor> getVendorById(Long vendorId) {
        return vendorRepository.findById(vendorId);
    }

}
