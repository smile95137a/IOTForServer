package backend.service;

import backend.entity.store.Store;
import backend.repo.StoreRepository;
import backend.utils.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    // Create a new store
    public Store createStore(Store store)
    {
        String s = RandomUtils.genRandom(24);
        store.setUid(s);
        return storeRepository.save(store);
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
    public Store updateStore(String uid, Store updatedStore) {
        return storeRepository.findByUid(uid).map(store -> {
            store.setName(updatedStore.getName());
            store.setAddress(updatedStore.getAddress());
            store.setVendor(updatedStore.getVendor());
            store.setPoolTables(updatedStore.getPoolTables());
            store.setCreateTime(updatedStore.getCreateTime());
            store.setCreateUserId(updatedStore.getCreateUserId());
            store.setUpdateTime(updatedStore.getUpdateTime());
            store.setUpdateUserId(updatedStore.getUpdateUserId());
            return storeRepository.save(store);
        }).orElseThrow(() -> new RuntimeException("Store not found with id: " + uid));
    }

    // Delete a store
    public void deleteStore(String uid) {
        storeRepository.deleteByUid(uid);
    }
}
