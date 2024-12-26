package backend.entity.store;

import backend.entity.poolTable.PoolTable;
import backend.entity.vendor.Vendor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stores")
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 分店名稱

    @Column(nullable = false)
    private String address; // 地址

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor; // 所屬廠商

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PoolTable> poolTables; // 分店中的桌台

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StoreEquipment> equipments; // 店家的设备

    @Column
    private LocalDateTime createTime;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;
}
