package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
@EqualsAndHashCode(exclude = {"regularTimeSlots", "discountTimeSlots"}) // 避免循環
@ToString(exclude = {"regularTimeSlots", "discountTimeSlots"}) // 避免循環
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
    @JsonBackReference
    private List<TimeSlot> regularTimeSlots = new ArrayList<>(); // 普通时段

    // 优惠时段列表
    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonBackReference
    private List<TimeSlot> discountTimeSlots = new ArrayList<>(); // 优惠时段

    @Column(nullable = false)
    private Integer regularRate; // 普通时段价格

    @Column(nullable = false)
    private Integer discountRate; // 优惠时段价格

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek, regularRate, discountRate); // 不包括与 TimeSlot 的关系
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StorePricingSchedule that = (StorePricingSchedule) o;
        return Objects.equals(dayOfWeek, that.dayOfWeek) &&
                Objects.equals(regularRate, that.regularRate) &&
                Objects.equals(discountRate, that.discountRate);
    }
}
