package com.frontend.pattern.game;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameRes;
import com.frontend.service.AbstractGameService;
import com.frontend.service.RouterService;
import com.frontend.utils.RandomUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ImmediateStartStrategy extends AbstractGameService implements GameStartStrategy {

    private final StoreRepository storeRepository;
    private final VendorRepository vendorRepository;

    public ImmediateStartStrategy(UserRepository userRepository,
                                  PoolTableRepository poolTableRepository,
                                  StoreRepository storeRepository,
                                  VendorRepository vendorRepository,
                                  GameRecordRepository gameRecordRepository,
                                  TableEquipmentRepository tableEquipmentRepository,
                                  RouterRepository routerRepository,
                                  RouterService routerService,
                                  StorePricingScheduleRepository storePricingScheduleRepository,
                                  GameOrderRepository gameOrderRepository,
                                  BookGameRepository bookGameRepository) {
        super(userRepository, poolTableRepository, storeRepository, vendorRepository, gameRecordRepository,
                tableEquipmentRepository, routerRepository, routerService, storePricingScheduleRepository,
                gameOrderRepository, bookGameRepository);
        this.storeRepository = storeRepository;
        this.vendorRepository = vendorRepository;
    }

    @Override
    @Transactional
    public GameRes startGame(GameReq gameReq, Long userId) throws Exception {
        // 1. 檢查用戶和桌台
        if (checkoutOrder()) {
            throw new Exception("有尚未結帳的球局，請先結帳後才能使用開台服務");
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new Exception("找不到用戶"));
        PoolTable poolTable = poolTableRepository.findByUid(gameReq.getPoolTableUId())
                .orElseThrow(() -> new Exception("找不到桌台"));
        if (checkPooltable(poolTable.getUid())) {
            throw new Exception("球局目前不開放使用，請換別桌進行球局");
        }

        Store store = storeRepository.findById(poolTable.getStore().getId())
                .orElseThrow(() -> new Exception("找不到店家"));
        Vendor vendor = vendorRepository.findById(store.getVendor().getId())
                .orElseThrow(() -> new Exception("找不到廠商"));

        // 2. 建立遊戲紀錄
        LocalDateTime now = LocalDateTime.now();
        GameRecord gameRecord = new GameRecord();
        gameRecord.setGameId(RandomUtils.genRandom(12));
        gameRecord.setStartTime(now);
        gameRecord.setUserUid(user.getUid());
        gameRecord.setPrice(0);
        gameRecord.setStatus("STARTED");
        gameRecord.setStoreId(store.getId());
        gameRecord.setStoreName(store.getName());
        gameRecord.setVendorId(vendor.getId());
        gameRecord.setVendorName(vendor.getName());
        gameRecord.setPoolTableId(poolTable.getId());
        gameRecord.setPoolTableName(poolTable.getTableNumber());
        gameRecordRepository.save(gameRecord);

        // 3. 更新桌台和設備、Router
        updatePoolTableStatus(poolTable, true);

        // 4. 回傳結果
        return new GameRes(gameRecord, "", 0, vendor, store.getContactPhone());
    }
}
