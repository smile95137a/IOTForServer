package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.repo.PoolTableRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.poolTable.PoolTableRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Autowired
    private StoreRepository storeRepository;

    public List<PoolTable> findByStoreUid(String storeUid) {
        Store store = storeRepository.findByUid(storeUid).get();
        return poolTableRepository.findByStoreId(store.getId());
    }

    public PoolTableRes getPoolTableById(String uid) {
        PoolTable poolTable = poolTableRepository.findByUid(uid).get();
        Integer dep = 0;
        if(poolTable != null) {
            Store store = storeRepository.findById(poolTable.getStore().getId()).get();
            dep = store.getDeposit();
        }
        PoolTableRes poolTableRes = new PoolTableRes();
        poolTableRes.setDeposit(dep);
        return poolTableRes;
    }
}
