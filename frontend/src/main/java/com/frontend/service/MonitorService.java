package com.frontend.service;

import com.frontend.entity.monitor.Monitor;
import com.frontend.entity.store.Store;
import com.frontend.repo.MonitorRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.req.monitor.MonitorReq;
import com.frontend.req.monitor.MonitorUpdateReq;
import com.frontend.res.monitor.MonitorRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MonitorService {

    @Autowired
    private MonitorRepository monitorRepository;
    @Autowired
    private StoreRepository storeRepository;

    // 新增監視器
    public Monitor createMonitor(MonitorReq req, Long userId) {
        Store store = storeRepository.findById(req.getStoreId())
                .orElseThrow(() -> new RuntimeException("Store not found"));

        Monitor monitor = new Monitor();
        monitor.setUid(UUID.randomUUID().toString());
        monitor.setName(req.getName());
        monitor.setNumber(req.getNumber());
        monitor.setStatus(false); // 預設狀態為 ACTIVE
        monitor.setStoreIP(req.getStoreIP());
        monitor.setCreateTime(LocalDateTime.now());
        monitor.setCreateUserId(userId);
        monitor.setStore(store);

        return monitorRepository.save(monitor);
    }

    // 更新監視器
    public Monitor updateMonitor(MonitorUpdateReq req, Long userId) {
        Monitor monitor = monitorRepository.findByUid(req.getUid())
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        if(req.getStoreIP() != null){
            monitor.setStoreIP(req.getStoreIP());
        }

        if (req.getNumber() != null) {
            monitor.setNumber(req.getNumber());
        }

        if (req.getStatus() != null) {
            monitor.setStatus(req.getStatus());
        }
        if (req.getName() != null) {
            monitor.setName(req.getName());
        }
        if (req.getStoreId() != null) {
            Store store = storeRepository.findById(req.getStoreId())
                    .orElseThrow(() -> new RuntimeException("Store not found"));
            monitor.setStore(store);
        }

        monitor.setUpdateTime(LocalDateTime.now());
        monitor.setUpdateUserId(userId);

        return monitorRepository.save(monitor);
    }


    // 刪除監視器
    public void deleteMonitor(Long id) {
        monitorRepository.deleteById(id);
    }

    // 根據店家 ID 查詢監視器並轉換為 Res
    public List<MonitorRes> getMonitorsByStoreId(Long id) {
        return monitorRepository.findByStoreId(id)
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    // 查詢所有監視器
    public List<MonitorRes> getAll() {
        return monitorRepository.findAll()
                .stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }
    private MonitorRes toRes(Monitor monitor) {
        MonitorRes res = new MonitorRes();
        res.setUid(monitor.getUid());
        res.setName(monitor.getName());
        res.setNumber(monitor.getNumber());
        res.setStatus(monitor.isStatus());
        res.setStoreIP(monitor.getStoreIP());
        if (monitor.getStore() != null) {
            res.setStoreId(monitor.getStore().getId());
            res.setStoreName(monitor.getStore().getName()); // 假設 Store 有 getName()
        }
        res.setId(monitor.getId());
        return res;
    }
}
