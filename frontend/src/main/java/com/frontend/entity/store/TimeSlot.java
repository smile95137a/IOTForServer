package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Objects;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "time_slots")
@EqualsAndHashCode(exclude = {"regularSchedule", "discountSchedule"})
@ToString(exclude = {"regularSchedule", "discountSchedule"})
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regular_schedule_id")
    @JsonBackReference(value = "regularTimeSlotReference")
    private StorePricingSchedule regularSchedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "discount_schedule_id")
    @JsonBackReference(value = "discountTimeSlotReference")
    private StorePricingSchedule discountSchedule;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean isDiscount;

    public TimeSlot(LocalTime startTime, LocalTime endTime, Boolean isDiscount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDiscount = isDiscount;
    }

    // 新增方法：获取关联的正确调度
    public StorePricingSchedule getAssociatedSchedule() {
        return isDiscount ? discountSchedule : regularSchedule;
    }
}
