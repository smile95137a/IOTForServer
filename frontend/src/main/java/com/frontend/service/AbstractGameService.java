package com.frontend.service;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.router.Router;
import com.frontend.entity.store.Store;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.user.User;
import com.frontend.repo.*;
import com.frontend.req.router.CircuitControlRequest;
import com.frontend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public abstract class AbstractGameService {
    protected final UserRepository userRepository;
    protected final PoolTableRepository poolTableRepository;
    protected final StoreRepository storeRepository;
    protected final VendorRepository vendorRepository;
    protected final GameRecordRepository gameRecordRepository;
    protected final TableEquipmentRepository tableEquipmentRepository;
    protected final RouterRepository routerRepository;
    protected final RouterService routerService;
    protected final StorePricingScheduleRepository storePricingScheduleRepository;
    protected final GameOrderRepository gameOrderRepository;
    protected final BookGameRepository bookGameRepository;

    /** 檢查營業時間 */
    protected void validateBusinessHours(Store store) throws Exception {
        LocalDate today = LocalDate.now();
        String currentDayString = today.getDayOfWeek().toString().toLowerCase();
        List<StorePricingSchedule> pricingSchedules = storePricingScheduleRepository.findByStoreId(store.getId());

        StorePricingSchedule schedule = pricingSchedules.stream()
                .filter(s -> s.getDayOfWeek().toLowerCase().equals(currentDayString))
                .findFirst()
                .orElseThrow(() -> new Exception("沒有找到當天的訊息"));

        LocalTime nowTime = LocalTime.now();
        if (nowTime.isBefore(schedule.getOpenTime()) || nowTime.isAfter(schedule.getCloseTime())) {
            throw new Exception("非營業時間，無法開台。營業時間為：" 
                    + schedule.getOpenTime() + " - " + schedule.getCloseTime());
        }
    }

    /** 更新桌台與設備狀態 */
    protected void updatePoolTableStatus(PoolTable poolTable, boolean isUse) {
        poolTable.setIsUse(isUse);
        poolTableRepository.save(poolTable);

        List<TableEquipment> equipments = tableEquipmentRepository.findByPoolTableId(poolTable.getId());
        for (TableEquipment table : equipments) {
            table.setStatus(isUse);
            tableEquipmentRepository.save(table);
        }

        List<Router> routers = routerRepository.findByPoolTables_Id(poolTable.getId());
        for(Router router : routers) {
            try {
                CircuitControlRequest request = new CircuitControlRequest();
                request.setRouterId(router.getId());
                request.setTargetStatus(isUse);
                request.setStoreId(router.getStore().getId());
                routerService.controlCircuit(request);
            } catch (Exception e) {
                System.err.println("控制 router 失敗: " + router.getId() + "，錯誤: " + e.getMessage());
            }
        }
    }
    /**
     * 檢查是否有未結帳的球局
     */
    protected boolean checkoutOrder() {
        Long userId = SecurityUtils.getSecurityUser().getId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("找不到用戶"));

        return gameRecordRepository.findByUserUidAndStatus(user.getUid(), "UNPAID")
                .stream()
                .anyMatch(record -> "UNPAID".equals(record.getStatus()));
    }

    /**
     * 檢查桌台是否可用
     */
    protected boolean checkPooltable(String poolTableUID) {
        PoolTable poolTable = poolTableRepository.findByUid(poolTableUID)
                .orElseThrow(() -> new RuntimeException("找不到桌台"));

        return !"AVAILABLE".equals(poolTable.getStatus());
    }

}
