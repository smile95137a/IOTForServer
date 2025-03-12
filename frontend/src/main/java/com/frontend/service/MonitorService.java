package com.frontend.service;

import com.frontend.entity.monitor.Monitor;
import com.frontend.entity.store.Store;
import com.frontend.repo.MonitorRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.req.monitor.MonitorReq;
import com.frontend.req.monitor.MonitorUpdateReq;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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
        monitor.setStatus(false); // 預設狀態為 ACTIVE
        monitor.setCreateTime(LocalDateTime.now());
        monitor.setCreateUserId(userId);
        monitor.setStore(store);

        return monitorRepository.save(monitor);
    }

    // 更新監視器
    public Monitor updateMonitor(MonitorUpdateReq req, Long userId) {
        Monitor monitor = monitorRepository.findByUid(req.getUid())
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

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


    // 取得特定商店的所有監視器
    public List<Monitor> getMonitorsByStoreUid(Long id) {
        return monitorRepository.findByStoreId(id);
    }


    // 刪除監視器
    public void deleteMonitor(Long id) {
        monitorRepository.deleteById(id);
    }
}
