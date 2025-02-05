package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.repo.PoolTableRepository;
import com.frontend.repo.StoreRepository;
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
}
