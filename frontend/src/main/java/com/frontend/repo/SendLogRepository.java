package com.frontend.repo;

import com.frontend.entity.topLog.SendLog;
import com.frontend.entity.topLog.TopLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SendLogRepository extends JpaRepository<SendLog, Long> {
    SendLog findByUserId(Long userId);
}
