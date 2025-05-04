package com.frontend.entity.store;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "global_pricing_overrides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GlobalPricingOverride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;  // 活動開始日期

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;    // 活動結束日期

    private Integer regularRate;  // 常規價格

    private Integer discountRate; // 折扣價格

    @OneToMany(mappedBy = "globalOverride", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GlobalTimeSlot> timeSlots = new ArrayList<>();
}
