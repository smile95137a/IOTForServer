package com.frontend.repo;

import backend.entity.user.LineUserActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LineUserActionLogRepository extends JpaRepository<LineUserActionLog, Long> {
}
