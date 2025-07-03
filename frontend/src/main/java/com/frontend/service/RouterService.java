package com.frontend.service;

import com.frontend.ISAPI.Modbus4jReadUtil;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.router.Router;
import com.frontend.entity.store.Store;
import com.frontend.factory.RouterFactory;
import com.frontend.repo.PoolTableRepository;
import com.frontend.repo.RouterRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.req.router.AddRouterRequest;
import com.frontend.req.router.CircuitControlRequest;
import com.frontend.req.router.RouterCircuitRequest;
import com.frontend.res.router.RouterResponse;
import com.frontend.utils.SecurityUtils;
import com.serotonin.modbus4j.ModbusFactory;
import com.serotonin.modbus4j.ModbusMaster;
import com.serotonin.modbus4j.ip.IpParameters;
import com.serotonin.modbus4j.locator.BaseLocator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RouterService {

    private final RouterRepository routerRepository;
    private final StoreRepository storeRepository;
    private final PoolTableRepository poolTableRepository;
    public RouterService(RouterRepository routerRepository,
                         StoreRepository storeRepository,
                         RouterFactory routerFactory, PoolTableRepository poolTableRepository) {
        this.routerRepository = routerRepository;
        this.storeRepository = storeRepository;
        this.poolTableRepository = poolTableRepository;
    }

    // 1. 新增 Router
    public Router addRouter(Long storeId, AddRouterRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        Router router = new Router();
        router.setStore(store);
        router.setUid(UUID.randomUUID().toString());
        router.setCreateTime(LocalDateTime.now());
        router.setCreateUserId(SecurityUtils.getSecurityUser().getId());
        // 設定迴路資訊
        router.setCircuitNumber(request.getCircuitNumber());
        router.setCircuitName(request.getCircuitName());
        router.setCircuitType(request.getCircuitType());
        router.setModbusAddress(request.getModbusAddress());
        router.setSlaveId(request.getSlaveId() != null ? request.getSlaveId() : 1);
        router.setIsControllable(request.getIsControllable() != null ? request.getIsControllable() : true);
        router.setRouterIP(request.getRouterIP());
        router.setRouterPort(request.getRouterPort());
        return routerRepository.save(router);
    }

    // 2. 取得某個店家的所有 Router
    public List<RouterResponse> getRoutersByStoreId(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        List<Router> routers = routerRepository.findByStoreId(storeId);
        return routers.stream()
                .map(router -> {
                    RouterResponse response = new RouterResponse();
                    response.setId(router.getId());
                    response.setEquipmentName(router.getEquipmentName());
                    response.setStatus(router.getStatus());
                    response.setUid(router.getUid());
                    response.setCreateTime(router.getCreateTime());
                    response.setUpdateTime(router.getUpdateTime());

                    // 新增迴路資訊
                    response.setCircuitNumber(router.getCircuitNumber());
                    response.setCircuitName(router.getCircuitName());
                    response.setCircuitType(router.getCircuitType());
                    response.setModbusAddress(router.getModbusAddress());
                    response.setSlaveId(router.getSlaveId());

                    response.setRouterPort(router.getRouterPort());
                    // 讀取目前迴路狀態
                    Boolean currentStatus = null;
                    try {
                        currentStatus = readCircuitStatus(router);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                    response.setIsControllable(currentStatus);
                    response.setCurrentCircuitStatus(currentStatus);

                    return response;
                })
                .toList();
    }

    // 新增：迴路控制功能
    @Transactional
    public boolean controlCircuit(CircuitControlRequest request) throws Exception {
        Store store = storeRepository.findById(request.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("Store not found"));

        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> new IllegalArgumentException("Router not found"));

        if (router.getModbusAddress() == null) {
            throw new IllegalStateException("Router 未設定 Modbus 地址");
        }
// 新增連線（每次操作都新建）
        ModbusMaster master = buildModbusMaster(store, router);
        master.init();
        try {
            int slaveId = router.getSlaveId() != null ? router.getSlaveId() : 1;
            var locator = BaseLocator.coilStatus(slaveId, router.getModbusAddress());

            master.setValue(locator, request.isTargetStatus());

            System.out.println("✅ 迴路控制成功 - " + router.getCircuitName() + ": " +
                    (request.isTargetStatus() ? "開啟" : "關閉"));
            return true;
        } catch (Exception e) {
            System.err.println("❌ 迴路控制失敗: " + e.getMessage());
            e.printStackTrace();
            return false;
        }finally {
            if (master != null) {
                try {
                    master.destroy(); // ✅ 自動清理
                } catch (Exception ignore) {}
            }
        }
    }

    private ModbusMaster buildModbusMaster(Store store, Router router) throws Exception {
        String ip = store.getStoreIP();
        int port = Integer.parseInt(router.getRouterPort());

        IpParameters ipParameters = new IpParameters();
        ipParameters.setHost(ip);
        ipParameters.setPort(port);
        ipParameters.setEncapsulated(false);

        ModbusFactory factory = new ModbusFactory();
        return factory.createTcpMaster(ipParameters, true);
    }



    private Boolean readCircuitStatus(Router router) throws Exception {
        Store store = router.getStore(); // 確保 Router 有關聯 Store
        if (store == null || store.getStoreIP() == null) {
            throw new IllegalStateException("Router 未關聯有效 Store 或 Store IP 為空");
        }
        ModbusMaster master = buildModbusMaster(store, router);
        if (router.getModbusAddress() == null) {
            return null;
        }

        try {


            // 每次建立新的 master 實例

            master.init();

            int slaveId = router.getSlaveId() != null ? router.getSlaveId() : 1;
            return Modbus4jReadUtil.readCoilStatus(
                    master,
                    slaveId,
                    router.getModbusAddress(),
                    "circuit_" + router.getId()
            );

        } catch (Exception e) {
            System.err.println("❌ 讀取迴路狀態失敗 - Router ID: " + router.getId() + ", 錯誤: " + e.getMessage());
            return null;
        }finally {
            if (master != null) {
                try {
                    master.destroy(); // ✅ 自動清理
                } catch (Exception ignore) {}
            }
        }
    }


    // 3. 更新 Router 的類型
    @Transactional
    public Router updateRouter(Long id, RouterCircuitRequest request) {
        Router router = routerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Router not found with id: " + id));

        router.setCircuitNumber(request.getCircuitNumber());
        router.setCircuitName(request.getCircuitName());
        router.setCircuitType(request.getCircuitType());
        router.setModbusAddress(request.getModbusAddress());
        router.setSlaveId(request.getSlaveId());
        router.setIsControllable(request.getIsControllable());
        router.setUpdateTime(LocalDateTime.now());
        router.setUpdateUserId(SecurityUtils.getSecurityUser().getId());
        router.setRouterIP(request.getRouterIP());
        router.setRouterPort(request.getRouterPort());
        return routerRepository.save(router);
    }


    // 4. 刪除 Router
    @Transactional
    public void deleteRouter(Long id) {
        Router router = routerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Router not found with id: " + id));

        // 解除 router 和 poolTables 的雙向關聯
        if (router.getPoolTables() != null) {
            for (PoolTable table : router.getPoolTables()) {
                table.getRouters().remove(router);
            }
            router.getPoolTables().clear();
        }

        routerRepository.delete(router);
    }



    // 5. 取得某個店家底下 Router 數量
    public int getRouterCountByStoreId(Long storeId) {
        return routerRepository.countByStoreId(storeId);
    }
}
