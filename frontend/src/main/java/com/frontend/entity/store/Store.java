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
    private Vendor vendor; // 所屬廠商

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
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

    @Column
    private Long lat;

    @Column
    private Long lon;

    // 新增欄位：一班時段的金額
    @Column
    private Integer regularRate;

    // 新增欄位：優惠時段的金額
    @Column
    private Integer discountRate;

    // 新增欄位：一班時段的日期（星期幾~星期幾）
    @Column
    private String regularDateRange;

    // 新增欄位：優惠時段的日期（星期幾~星期幾）
    @Column
    private String discountDateRange;

    // 新增欄位：一班時段的時間（18:00-22:00）
    @Column
    private String regularTimeRange;

    // 新增欄位：優惠時段的時間（08:00-18:00）
    @Column
    private String discountTimeRange;

    @Column //押金
    private Integer deposit;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Store store = (Store) o;
        return Objects.equals(uid, store.uid);  // 使用 uid 比較物件
    }

    // 覆寫 hashCode() 方法
    @Override
    public int hashCode() {
        return Objects.hash(uid);  // 根據 uid 計算 hashCode，確保與 equals 一致
    }
}
