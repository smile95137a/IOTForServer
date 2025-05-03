package com.frontend.entity.recharge;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recharge_promotion")
@Data
public class RechargePromotion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String name;
    @Column
    private LocalDate startDate;
    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status;

    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime = LocalDateTime.now();
    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime = LocalDateTime.now();

    @OneToMany(mappedBy = "promotion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RechargePromotionDetail> details = new ArrayList<>();
}