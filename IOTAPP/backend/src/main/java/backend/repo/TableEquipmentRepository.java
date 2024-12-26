package backend.repo;

import backend.entity.poolTable.TableEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableEquipmentRepository extends JpaRepository<TableEquipment, Long> {
    // 你可以在这里添加自定义查询方法
}
