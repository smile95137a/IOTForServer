package com.frontend.repo;

import com.frontend.entity.store.StoreEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreEquipmentRepository extends JpaRepository<StoreEquipment, Long> {
    List<StoreEquipment> findByStatus(Boolean status);
    List<StoreEquipment> findByStoreId(Long storeId);
}