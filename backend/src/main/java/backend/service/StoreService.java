package backend.service;

import backend.entity.store.Store;
import backend.repo.StoreRepository;
import backend.req.store.StoreReq;
import backend.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // Create a new store
    public Store createStore(StoreReq storeReq , Long id) {
        Store store = convertToEntity(storeReq);
        store.setUid(RandomUtils.genRandom(24)); // 生成唯一 UID
        store.setCreateTime(LocalDateTime.now());
        store.setCreateUserId(id);
        return storeRepository.save(store);
    }

    private Store convertToEntity(StoreReq req) {
        Store store = new Store();
        store.setName(req.getName());
        store.setAddress(req.getAddress());
        store.setLat(req.getLat());
        store.setLon(req.getLon());
        store.setRegularRate(req.getRegularRate());
        store.setDiscountRate(req.getDiscountRate());
        store.setDeposit(req.getDeposit());

        // 拼接日期范围
        store.setRegularDateRange(req.getRegularDateRangeStart() + " ~ " + req.getRegularDateRangeEnd());
        store.setDiscountDateRange(req.getDiscountDateRangeStart() + " ~ " + req.getDiscountDateRangeEnd());

        // 拼接时间范围
        store.setRegularTimeRange(req.getRegularTimeRangeStart() + " - " + req.getRegularTimeRangeEnd());
        store.setDiscountTimeRange(req.getDiscountTimeRangeStart() + " - " + req.getDiscountTimeRangeEnd());

        if (req.getVendor() != null) {
            store.setVendor(req.getVendor());
        }
        // 设备和桌台（如果有的话）
        if (req.getEquipments() != null) {
            store.setEquipments(req.getEquipments());
        }
        if (req.getPoolTables() != null) {
            store.setPoolTables(req.getPoolTables());
        }

        return store;
    }


    // Retrieve a store by ID
    public Optional<Store> getStoreById(String uid) {
        return storeRepository.findByUid(uid);
    }

    // Retrieve all stores
    public List<Store> getAllStores() {
        return storeRepository.findAll();
    }

    // Update a store
    public Store updateStore(String uid, StoreReq storeReq , Long id) {
        return storeRepository.findByUid(uid).map(store -> {
            store.setName(storeReq.getName());
            store.setAddress(storeReq.getAddress());
            store.setLat(storeReq.getLat());
            store.setLon(storeReq.getLon());
            store.setRegularRate(storeReq.getRegularRate());
            store.setDiscountRate(storeReq.getDiscountRate());
            store.setDeposit(storeReq.getDeposit());

            // 更新日期范围
            store.setRegularDateRange(storeReq.getRegularDateRangeStart() + " ~ " + storeReq.getRegularDateRangeEnd());
            store.setDiscountDateRange(storeReq.getDiscountDateRangeStart() + " ~ " + storeReq.getDiscountDateRangeEnd());

            // 更新时间范围
            store.setRegularTimeRange(storeReq.getRegularTimeRangeStart() + " - " + storeReq.getRegularTimeRangeEnd());
            store.setDiscountTimeRange(storeReq.getDiscountTimeRangeStart() + " - " + storeReq.getDiscountTimeRangeEnd());

            // 设备和桌台
            if (storeReq.getVendor() != null) {
                store.setVendor(storeReq.getVendor());
            }
            if (storeReq.getEquipments() != null) {
                store.setEquipments(storeReq.getEquipments());
            }
            if (storeReq.getPoolTables() != null) {
                store.setPoolTables(storeReq.getPoolTables());
            }

            // 更新修改时间 & 用户
            store.setUpdateTime(LocalDateTime.now());  // 设置当前时间
            store.setUpdateUserId(id);

            return storeRepository.save(store);
        }).orElseThrow(() -> new RuntimeException("Store not found with uid: " + uid));
    }


    // Delete a store
    public void deleteStore(String uid) {
        storeRepository.deleteByUid(uid);
    }
}
