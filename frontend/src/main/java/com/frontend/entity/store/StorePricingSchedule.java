package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
public class StorePricingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference
    private Store store; // 关联的店家

    @Column(nullable = false)
    private String dayOfWeek; // 星期几 (例如: MONDAY)

    // 普通时段列表
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeSlot> regularTimeSlots = new ArrayList<>(); // 普通时段

    // 优惠时段列表
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeSlot> discountTimeSlots = new ArrayList<>(); // 优惠时段

    @Column(nullable = false)
    private Integer regularRate; // 普通时段价格

    @Column(nullable = false)
    private Integer discountRate; // 优惠时段价格
}
