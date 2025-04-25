package com.frontend.entity.router;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.frontend.entity.equipment.Equipment;
import com.frontend.entity.store.Store;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 使用 JOINED 策略，讓子類別建立獨立的表
@Table(name = "routers")
public abstract class Router extends Equipment {
////////////給碩哥
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonBackReference("routerReference")
    private Store store;


    @Column
    private Long routerNumber;

    public void connect() {
        System.out.println("Connecting " + getClass().getSimpleName() + "...");
    }

    public void disconnect() {
        System.out.println("Disconnecting " + getClass().getSimpleName() + "...");
    }
}
