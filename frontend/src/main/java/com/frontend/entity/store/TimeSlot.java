package com.frontend.entity.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "time_slots")
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private StorePricingSchedule schedule; // 關聯的定價時段

    @Column(nullable = false)
    private LocalTime startTime; // 開始時間

    @Column(nullable = false)
    private LocalTime endTime; // 結束時間

    @Column(nullable = false)
    private Boolean isDiscount; // 是否是優惠時段
}
