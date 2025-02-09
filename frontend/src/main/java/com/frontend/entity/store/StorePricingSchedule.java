package com.frontend.entity.store;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "store_pricing_schedules")
public class StorePricingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "store_id", nullable = false)
    private Store store; // 關聯的店家

    @Column(nullable = false)
    private String dayOfWeek; // 星期幾 (例如: MONDAY)

    @Column(nullable = false)
    private String regularStartTime; // 一般時段開始時間 (例如: 08:00)

    @Column(nullable = false)
    private String regularEndTime; // 一般時段結束時間 (例如: 18:00)

    @Column(nullable = false)
    private Integer regularRate; // 一般時段金額

    @Column
    private String discountStartTime; // 優惠時段開始時間 (可為 NULL)

    @Column
    private String discountEndTime; // 優惠時段結束時間 (可為 NULL)

    @Column
    private Integer discountRate; // 優惠時段金額 (可為 NULL)
}
