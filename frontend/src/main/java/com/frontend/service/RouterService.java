package com.frontend.service;

import com.frontend.ISAPI.Modbus4jReadUtil;
import com.frontend.entity.router.Router;
import com.frontend.entity.store.Store;
import com.frontend.factory.RouterFactory;
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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RouterService {

    private final RouterRepository routerRepository;
    private final StoreRepository storeRepository;

    private ModbusMaster master;

    @PostConstruct
    public void initModbus() {
        try {
            IpParameters ipParameters = new IpParameters();
            ipParameters.setHost("192.168.1.108");
            ipParameters.setPort(502);
            ipParameters.setEncapsulated(false);

            ModbusFactory modbusFactory = new ModbusFactory();
            master = modbusFactory.createTcpMaster(ipParameters, true);
            master.init();
            System.out.println("Modbus 連線初始化成功");
        } catch (Exception e) {
            System.err.println("Modbus 初始化失敗: " + e.getMessage());
        }
    }

    public RouterService(RouterRepository routerRepository,
                         StoreRepository storeRepository,
                         RouterFactory routerFactory) {
        this.routerRepository = routerRepository;
        this.storeRepository = storeRepository;
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
                    response.setIsControllable(router.getIsControllable());
                    response.setRouterPort(router.getRouterPort());
                    // 讀取目前迴路狀態
                    Boolean currentStatus = readCircuitStatus(router);
                    response.setCurrentCircuitStatus(currentStatus);

                    return response;
                })
                .toList();
    }

    // 新增：迴路控制功能
    @Transactional
    public boolean controlCircuit(CircuitControlRequest request) {
        Router router = routerRepository.findById(request.getRouterId())
                .orElseThrow(() -> new IllegalArgumentException("Router not found with id: " + request.getRouterId()));

        if (!router.getIsControllable()) {
            throw new IllegalStateException("此迴路不可控制");
        }

        if (router.getModbusAddress() == null) {
            throw new IllegalStateException("Router 未設定 Modbus 地址");
        }

        try {
            int slaveId = router.getSlaveId() != null ? router.getSlaveId() : 1;

            // ✅ 正確建構 BaseLocator
            var locator = com.serotonin.modbus4j.locator.BaseLocator.coilStatus(slaveId, router.getModbusAddress());

            // ✅ 寫入 true / false 到 coil
            master.setValue(locator, request.getTargetStatus());

            System.out.println("迴路控制成功 - " + router.getCircuitName() +
                    " (" + router.getCircuitNumber() + "): " +
                    (request.getTargetStatus() ? "開啟" : "關閉"));
            return true;

        } catch (Exception e) {
            System.err.println("迴路控制失敗: " + e.getMessage());
            return false;
        }
    }


    private Boolean readCircuitStatus(Router router) {
        if (router.getModbusAddress() == null || master == null) {
            return null;
        }

        try {
            int slaveId = router.getSlaveId() != null ? router.getSlaveId() : 1;
            Boolean status = Modbus4jReadUtil.readCoilStatus(master, slaveId,
                    router.getModbusAddress(),
                    "circuit_" + router.getId());
            return status;
        } catch (Exception e) {
            System.err.println("讀取迴路狀態失敗 - Router ID: " + router.getId() + ", 錯誤: " + e.getMessage());
            return null;
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
        if (!routerRepository.existsById(id)) {
            throw new IllegalArgumentException("Router not found with id: " + id);
        }
        routerRepository.deleteById(id);
    }

    // 5. 取得某個店家底下 Router 數量
    public int getRouterCountByStoreId(Long storeId) {
        return routerRepository.countByStoreId(storeId);
    }
}
