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
    @JoinColumn(name = "schedule_id", nullable = false)
    private StorePricingSchedule schedule;

    @Column(nullable = false)
    private Boolean isDiscount;

    @Column(nullable = false)
    private Integer rate;

    @Column(nullable = false)
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    @Column(nullable = false)
    private LocalTime endTime;


    public TimeSlot(LocalTime startTime, LocalTime endTime, Boolean isDiscount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDiscount = isDiscount;
    }
}
