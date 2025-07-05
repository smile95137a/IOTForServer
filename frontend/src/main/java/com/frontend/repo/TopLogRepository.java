package com.frontend.repo;

import com.frontend.entity.topLog.TopLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopLogRepository extends JpaRepository<TopLog, Long> {
    TopLog findByUserId(Long userId);
}
