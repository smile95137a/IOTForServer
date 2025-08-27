package com.frontend.pattern.game;

import com.frontend.entity.game.BookGame;
import com.frontend.entity.game.GameOrder;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BookingStartStrategy extends AbstractGameService implements GameStartStrategy {

    public BookingStartStrategy(UserRepository userRepository,
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
    }

    @Override
    @Transactional
    public GameRes startGame(GameReq gameReq, Long userId) throws Exception {
        // 1. 基本檢查
        if (checkoutOrder()) {
            throw new Exception("有尚未結帳的球局，請先結帳後才能使用開台服務");
        }

        PoolTable poolTable = poolTableRepository.findById(gameReq.getPoolTableId())
                .orElseThrow(() -> new Exception("找不到桌台"));
        if (checkPooltable(poolTable.getUid())) {
            throw new Exception("球局目前不開放使用，請換別桌進行球局");
        }

        GameRecord gameRecord = gameRecordRepository.findByGameId(gameReq.getGameId());
        if (gameRecord == null || !"BOOK".equals(gameRecord.getStatus())) {
            throw new Exception("無預定球局");
        }

        GameOrder gameOrder = gameOrderRepository.findByGameId(gameReq.getGameId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = gameOrder.getStartTime();
        LocalDateTime endTime = gameOrder.getEndTime();

        if (now.isBefore(startTime)) {
            throw new Exception("未達預約時間");
        } else if (now.isAfter(endTime)) {
            System.out.println("已超過預約時間");
        }

        // 2. 更新遊戲紀錄與 BookGame
        gameRecord.setStatus("STARTED");
        gameRecord.setStartTime(now);
        gameRecordRepository.save(gameRecord);

        gameOrder.setStartTime(now);
        gameOrderRepository.save(gameOrder);

        BookGame bookGame = bookGameRepository.findByGameId(gameReq.getGameId());
        if (bookGame != null) {
            bookGame.setStatus("COMPLETE");
            bookGameRepository.save(bookGame);
        }

        // 3. 開啟桌台
        updatePoolTableStatus(poolTable, true);

        // 4. 取得店家與廠商
        Store store = poolTable.getStore();
        Vendor vendor = vendorRepository.findById(store.getVendor().getId())
                .orElseThrow(() -> new Exception("找不到廠商"));

        // 5. 回傳結果
        return new GameRes(gameRecord, "", 0, vendor, store.getContactPhone());
    }
}
