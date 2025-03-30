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
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonBackReference(value = "timeSlotReference")
    private StorePricingSchedule schedule;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @Column(nullable = false)
    private Boolean isDiscount;
}
