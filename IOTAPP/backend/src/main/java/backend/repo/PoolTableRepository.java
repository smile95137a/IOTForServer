package backend.repo;

import backend.entity.poolTable.PoolTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PoolTableRepository extends JpaRepository<PoolTable, Long> {
    Optional<PoolTable> findByUid(String uuid);

    void deleteByUid(String uid);
}
