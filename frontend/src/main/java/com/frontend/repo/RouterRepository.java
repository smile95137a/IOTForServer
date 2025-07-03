package com.frontend.repo;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.router.Router;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RouterRepository extends JpaRepository<Router, Long> {

    List<Router> findByStoreId(Long storeId);

    int countByStoreId(Long storeId);

    Optional<Router> findFirstByStoreIdAndSlaveId(Long storeId, int slave);

    List<Router> findByPoolTables_Id(Long poolTableId);
}
