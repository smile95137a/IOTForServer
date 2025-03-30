package com.frontend.service;

import com.frontend.entity.router.Router;
import com.frontend.entity.store.Store;
import com.frontend.enums.RouterType;
import com.frontend.factory.RouterFactory;
import com.frontend.repo.RouterRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.res.router.RouterResponse;
import com.frontend.utils.SecurityUtils;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RouterService {

    private final RouterRepository routerRepository;
    private final StoreRepository storeRepository;
    private final RouterFactory routerFactory;

    public RouterService(RouterRepository routerRepository,
                         StoreRepository storeRepository,
                         RouterFactory routerFactory) {
        this.routerRepository = routerRepository;
        this.storeRepository = storeRepository;
        this.routerFactory = routerFactory;
    }

    // 1. 新增 Router
    public Router addRouter(Long storeId, RouterType routerType , Long number) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));

        Router router = routerFactory.createRouter(routerType);
        router.setStore(store);
        router.setEquipmentName(String.valueOf(routerType));
        router.setStatus(true);
        router.setUid(UUID.randomUUID().toString());
        router.setCreateTime(LocalDateTime.now());
        router.setCreateUserId(SecurityUtils.getSecurityUser().getId());
        router.setRouterNumber(number);
        router.connect();

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
                    response.setRouterNumber(router.getRouterNumber());
                    response.setCreateTime(router.getCreateTime());
                    response.setUpdateTime(router.getUpdateTime());
                    return response;
                })
                .toList();
    }


    // 3. 更新 Router 的類型
    @Transactional
    public Router updateRouter(Long id, String routerType, Long number) {
        Router router = routerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Router not found with id: " + id));

        router.setEquipmentName(routerType);
        router.setRouterNumber(number);
        router.setUpdateTime(LocalDateTime.now());
        router.setUpdateUserId(SecurityUtils.getSecurityUser().getId());
        if(router.getStatus() == true) {
            router.setStatus(false);
        }else{
            router.setStatus(true);
        }

        // 如果 connect() 是更新的一部分，并且有副作用，保留它
        router.connect();

        return routerRepository.save(router); // 直接保存更新后的对象
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
