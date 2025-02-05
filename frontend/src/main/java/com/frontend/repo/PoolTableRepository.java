package com.frontend.repo;

import com.frontend.entity.poolTable.PoolTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoolTableRepository extends JpaRepository<PoolTable, Long> {

    List<PoolTable> findByStoreId(Long storeId);

}
