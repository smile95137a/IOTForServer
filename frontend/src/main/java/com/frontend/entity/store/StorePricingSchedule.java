package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
@EqualsAndHashCode(exclude = {"regularTimeSlots", "discountTimeSlots"})
@ToString(exclude = {"regularTimeSlots", "discountTimeSlots"})
public class StorePricingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    @JsonBackReference("defaultReference")
    private Store store;

    @Column(nullable = false)
    private String dayOfWeek;

    // 增加查询方法，帮助关联和查询
    public List<TimeSlot> getAllTimeSlots() {
        List<TimeSlot> allTimeSlots = new ArrayList<>();
        if (regularTimeSlots != null) {
            allTimeSlots.addAll(regularTimeSlots);
        }
        if (discountTimeSlots != null) {
            allTimeSlots.addAll(discountTimeSlots);
        }
        return allTimeSlots;
    }

    // 根据是否折扣获取对应时间段
    public List<TimeSlot> getTimeSlotsByDiscountStatus(boolean isDiscount) {
        return getAllTimeSlots().stream()
                .filter(slot -> slot.getIsDiscount() == isDiscount)
                .collect(Collectors.toList());
    }

    @OneToMany(mappedBy = "regularSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "regularTimeSlotReference")
    private List<TimeSlot> regularTimeSlots = new ArrayList<>();

    @OneToMany(mappedBy = "discountSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference(value = "discountTimeSlotReference")
    private List<TimeSlot> discountTimeSlots = new ArrayList<>();

    @Column(nullable = false)
    private Integer regularRate;

    @Column(nullable = false)
    private Integer discountRate;
}
