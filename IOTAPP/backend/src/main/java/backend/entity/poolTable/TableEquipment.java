package backend.entity.poolTable;

import backend.entity.equipment.Equipment;
import backend.entity.poolTable.PoolTable;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "table_equipments")
public class TableEquipment extends Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pool_table_id", nullable = false)
    private PoolTable poolTable; // 所屬桌台

    @Column
    private String description; // 設備描述或備註

}
