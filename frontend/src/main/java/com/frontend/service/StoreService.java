package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.enums.PoolTableStatus;
import com.frontend.repo.StoreRepository;
import com.frontend.res.store.StoreRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.frontend.entity.store.StorePricingSchedule;
import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository;

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

        // 確保處理 regularRate 和 discountRate 等邏輯
        Integer regularRate = store.getPricingSchedules().stream()
                .map(StorePricingSchedule::getRegularRate)
                .max(Integer::compareTo)
                .orElse(0);

        Integer discountRate = store.getPricingSchedules().stream()
                .map(StorePricingSchedule::getDiscountRate)
                .max(Integer::compareTo)
                .orElse(0);

        String regularTimeRange = store.getPricingSchedules().stream()
                .map(schedule -> schedule.getRegularStartTime() + "-" + schedule.getRegularEndTime())
                .findFirst()
                .orElse("");

        return new StoreRes(
                store.getId(),
                store.getUid(),
                store.getAddress(),
                store.getName(),
                availableCount,
                inUseCount,
                store.getLat(),
                store.getLon(),
                store.getDeposit(),
                regularRate,
                discountRate,
                regularTimeRange
        );
    }

}
