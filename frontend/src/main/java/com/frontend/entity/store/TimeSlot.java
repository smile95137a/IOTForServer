package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "time_slots")
@EqualsAndHashCode(exclude = "schedule") // 避免雙向調用
@ToString(exclude = "schedule") // 避免循環輸出
public class TimeSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    @JsonManagedReference
    private StorePricingSchedule schedule; // 關聯的定價時段

    @Column(nullable = false)
    private LocalTime startTime; // 開始時間

    @Column(nullable = false)
    private LocalTime endTime; // 結束時間

    @Column(nullable = false)
    private Boolean isDiscount; // 是否是優惠時段

    public TimeSlot(LocalTime startTime, LocalTime endTime, Boolean isDiscount) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.isDiscount = isDiscount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime, endTime, isDiscount);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeSlot timeSlot = (TimeSlot) o;
        return Objects.equals(startTime, timeSlot.startTime) &&
                Objects.equals(endTime, timeSlot.endTime) &&
                Objects.equals(isDiscount, timeSlot.isDiscount);
    }
}
