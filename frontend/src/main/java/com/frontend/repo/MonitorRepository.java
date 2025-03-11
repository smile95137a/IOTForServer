package com.frontend.repo;

import com.frontend.entity.monitor.Monitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitorRepository extends JpaRepository<Monitor, Long> {

    // 透過 storeId 查詢所有監視器
    List<Monitor> findByStoreId(Long storeId);

    Optional<Monitor> findByUid(String uid);

}
