package com.frontend.repo;

import com.frontend.entity.poolTable.TableEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TableEquipmentRepository extends JpaRepository<TableEquipment, Long> {
    List<TableEquipment> findByStatus(Boolean status);
}