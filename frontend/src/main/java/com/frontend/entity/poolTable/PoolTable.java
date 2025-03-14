package com.frontend.entity.poolTable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.frontend.entity.store.Store;
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
@Table(name = "pool_tables")
public class PoolTable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uid;

    @Column(nullable = false)
    private String tableNumber; // 桌台編號

    @Column(nullable = false)
    private String status; // 桌台狀態 (例如 "AVAILABLE", "IN_USE")

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference
    private Store store; // 所屬分店

//    @OneToMany(mappedBy = "poolTable", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<TableEquipment> tableEquipments; // 桌台設備設定

    @Column
    private LocalDateTime createTime;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;

    @Column
    private Boolean isUse;
}