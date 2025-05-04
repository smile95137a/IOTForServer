package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.store.GlobalTimeSlot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "global_pricing_overrides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPricingOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDate endDate;

    private Integer regularRate;

    private Integer discountRate;

    @OneToMany(mappedBy = "globalOverride", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GlobalTimeSlot> timeSlots = new ArrayList<>();

    // ✅ 新增：關聯到 Store
    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;

    @Column(name = "is_global")
    private Boolean isGlobal = false;

}
