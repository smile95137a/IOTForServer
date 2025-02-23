package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.vendor.Vendor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Objects;
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

    @Column
    private String uid;

    @Column(nullable = false)
    private String name; // 分店名稱

    @Column(nullable = false)
    private String address; // 地址

    @ManyToOne
    @JoinColumn(name = "vendor_id", nullable = false)
    @JsonBackReference
    private Vendor vendor;
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<PoolTable> poolTables; // 分店中的桌台


    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<StorePricingSchedule> pricingSchedules; // 店家的時段與費用設定

    @Column
    private String imgUrl;

    @Column
    private LocalDateTime createTime;

    @Column
    private Long createUserId;

    @Column
    private LocalDateTime updateTime;

    @Column
    private Long updateUserId;

    @Column
    private String lat;

    @Column
    private String lon;

    @Column // 押金
    private Integer deposit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(uid, store.uid);  // 使用 uid 比較物件
    }

    @Override
    public int hashCode() {
        return Objects.hash(uid);  // 根據 uid 計算 hashCode，確保與 equals 一致
    }
}
