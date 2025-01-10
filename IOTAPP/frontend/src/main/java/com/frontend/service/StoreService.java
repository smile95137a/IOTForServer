package com.frontend.service;

import com.frontend.repo.StoreRepository;
import com.frontend.res.store.StoreRes;
import com.model.entity.store.Store;
import com.model.enums.PoolTableStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public Optional<Store> getStoreById(String uid) {
        return storeRepository.findByUid(uid);
    }

    public List<Store> initCreateStore(List<Store> stores) {
        return storeRepository.saveAll(stores);
    }

    public Optional<List<StoreRes>> countAvailableAndInUseByUid(String uid){
        return storeRepository.countAvailableAndInUseByUid(uid, PoolTableStatus.AVAILABLE.name());
    }

}
