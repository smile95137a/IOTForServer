package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.repo.PoolTableRepository;
import com.frontend.repo.StorePricingScheduleRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.poolTable.PoolTableRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PoolTableService {

    @Autowired
    private PoolTableRepository poolTableRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private StorePricingScheduleRepository storePricingScheduleRepository;

    // 获取今天是星期几
    private String getDayOfWeek() {
        LocalDate today = LocalDate.now();
        return today.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase(); // 例如 "MONDAY"
    }


    public List<PoolTable> findByStoreUid(String storeUid) {
        Store store = storeRepository.findByUid(storeUid).get();
        List<PoolTable> poolTables = poolTableRepository.findByStoreId(store.getId());

        return poolTables;
    }


    public PoolTableRes getPoolTableById(String uid) throws Exception {
        PoolTable poolTable = poolTableRepository.findByUid(uid).get();
        Integer dep = 0;
        if(poolTable != null) {
            Store store = storeRepository.findById(poolTable.getStore().getId()).get();
            dep = store.getDeposit();
        }

        if(poolTable.getIsUse() == true){
            throw new Exception("很遺憾!本桌台已被其他用戶使用中，請換個桌台吧!");
        }

        PoolTableRes poolTableRes = new PoolTableRes();
        poolTableRes.setDeposit(dep);
        return poolTableRes;
    }
}
