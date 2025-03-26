package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
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

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private Set<TimeSlot> timeSlots = new HashSet<>();

    @Column(nullable = false)
    private Integer regularRate;

    @Column(nullable = false)
    private Integer discountRate;
}
