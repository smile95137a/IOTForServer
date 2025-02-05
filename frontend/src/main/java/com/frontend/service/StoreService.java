package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.StoreRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

    public List<Store> initCreateStore(List<Store> stores) {
        return storeRepository.saveAll(stores);
    }

    public Optional<List<StoreRes>> countAvailableAndInUseByUid(String uid){
        return storeRepository.countAvailableAndInUseByUid(uid, PoolTableStatus.AVAILABLE.name());
    }

    public List<StoreRes> findAll() {
        return storeRepository.findAll().stream()
                .map(StoreService::convert) // 使用 convert 方法转换 Store -> StoreRes
                .toList();
    }


    public static StoreRes convert(Store store) {
        if (store == null) {
            return null;
        }

        long availableCount = store.getPoolTables().stream()
                .filter(poolTable -> !poolTable.getIsUse())
                .count();

        long inUseCount = store.getPoolTables().stream()
                .filter(PoolTable::getIsUse)
                .count();

        return new StoreRes(
                store.getId(),
                store.getUid(),
                store.getAddress(),
                store.getName(),
                availableCount,
                inUseCount
        );
    }
}
