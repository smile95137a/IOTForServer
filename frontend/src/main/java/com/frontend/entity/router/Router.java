package com.frontend.entity.router;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.frontend.entity.equipment.Equipment;
import com.frontend.entity.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 使用 JOINED 策略，讓子類別建立獨立的表
@Table(name = "routers")
@Where(clause = "is_deleted = false")
public class Router extends Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonBackReference("routerReference")
    private Store store;

    @Column
    private Long routerNumber;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "circuit_number")
    private Integer circuitNumber;        // 迴路編號 (1, 2, 3...)

    @Column(name = "circuit_name")
    private String circuitName;           // 迴路名稱 (電燈, 抽氣, 冷氣...)

    @Column(name = "circuit_type")
    private String circuitType;           // 迴路類型 (LIGHTING, VENTILATION, COOLING)

    @Column(name = "modbus_address")
    private Integer modbusAddress;        // Modbus 地址 (0, 1, 6...)

    @Column(name = "slave_id")
    private Integer slaveId = 1;          // Slave ID，預設為 1

    @Column(name = "is_controllable")
    private Boolean isControllable = true; // 是否可控制開關

    @Column
    private String routerIP;

    @Column
    private String routerPort;

    public void connect() {
        System.out.println("Connecting " + getClass().getSimpleName() + "...");
    }

    public void disconnect() {
        System.out.println("Disconnecting " + getClass().getSimpleName() + "...");
    }
}
