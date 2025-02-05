package src.main.java.backend.repo;

import src.main.java.backend.entity.poolTable.TableEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableEquipmentRepository extends JpaRepository<TableEquipment, Long> {
    // 你可以在这里添加自定义查询方法

    Optional<TableEquipment> findByUid(String uuid);

    void deleteByUid(String uuid);
}
