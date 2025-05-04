package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.router.Router;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stores")
@Where(clause = "is_deleted = false")
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
    @JsonBackReference("vendorReference")
    private Vendor vendor;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("poolTableReference")
    private Set<PoolTable> poolTables;


    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, fetch = FetchType.LAZY , orphanRemoval = true)
    @JsonManagedReference("pricingScheduleReference")
    private Set<StorePricingSchedule> pricingSchedules;

    @Column
    private String imgUrl;

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
    private String lat;

    @Column
    private String lon;

    @Column // 押金
    private Integer deposit;

    @Column(columnDefinition = "LONGTEXT")
    private String hint;

    @Column
    private String contactPhone;

    @Column
    private Integer bookTime;

    @Column
    private Integer cancelBookTime;

    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("routerReference")
    private Set<Router> routers;


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @JsonBackReference("userReference")
    private User user;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // ✅ 預設為 false

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
