package backend.entity.poolTable;

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
public class TableEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pool_table_id", nullable = false)
    private PoolTable poolTable; // 所屬桌台

    @Column(nullable = false)
    private String equipmentName; // 設備名稱 (例如 "燈光", "空調")

    @Column(nullable = false)
    private String status; // 設備狀態 (例如 "ON", "OFF")

    @Column
    private LocalTime autoStartTime; // 自動開啟時間

    @Column
    private LocalTime autoStopTime; // 自動關閉時間

    @Column
    private String description; // 設備描述或備註

    @Column
    private LocalDateTime createTime;

    @Column
    private String uid;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;
}
