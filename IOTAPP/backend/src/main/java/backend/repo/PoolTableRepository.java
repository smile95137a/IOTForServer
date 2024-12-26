package backend.repo;

import backend.entity.poolTable.PoolTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolTableRepository extends JpaRepository<PoolTable, Long> {
}
