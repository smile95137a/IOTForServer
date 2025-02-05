package com.frontend.entity.store;

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
@Table(name = "store_equipment")
public class StoreEquipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uid;

    @Column(nullable = false, unique = true)
    private String name; // 设备名称

    @Column(nullable = false)
    private String status; // 设备状态 (例如 "ON", "OFF")

    @Column
    private LocalTime autoStartTime; // 自动开启时间

    @Column
    private LocalTime autoStopTime; // 自动关闭时间

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 所属店家

    @Column
    private LocalDateTime createTime;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;
}
