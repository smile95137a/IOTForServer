package com.frontend.entity.poolTable;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pool_tables")
@Where(clause = "is_deleted = false")
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
    @JoinColumn(name = "store_id")
    @JsonBackReference("poolTableReference")
    private Store store;

//    @OneToMany(mappedBy = "poolTable", cascade = CascadeType.ALL, orphanRemoval = true)
//    private Set<TableEquipment> tableEquipments; // 桌台設備設定

    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    @Column
    private Long createUserId;

    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;

    @Column
    private Boolean isUse;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
}