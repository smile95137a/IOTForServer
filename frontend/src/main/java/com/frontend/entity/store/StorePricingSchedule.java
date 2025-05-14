package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Where;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
@EqualsAndHashCode(exclude = {"timeSlots"})
@ToString(exclude = {"timeSlots"})
@Where(clause = "is_deleted = false")
    public class StorePricingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id")
    @JsonBackReference("pricingScheduleReference")
    private Store store;


    @Column(nullable = false)
    private String dayOfWeek;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY , orphanRemoval = true)
    @JsonManagedReference(value = "timeSlotReference")
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @JsonFormat(pattern = "HH:mm")
    private LocalTime openTime;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime closeTime;

    @Column(nullable = false)
    private Double regularRate;

    @Column(nullable = false)
        private Double discountRate;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

}
