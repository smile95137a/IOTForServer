package com.frontend.entity.recharge;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.enums.PromotionStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "recharge_promotion_detail")
@Data
public class RechargePromotionDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id")
    private RechargePromotion promotion;

    @Column(nullable = false)
    private Integer rechargeAmount;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status;
    @Column
    private Integer bonusAmount;
    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime = LocalDateTime.now();
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    @Column(nullable = false)
    private LocalDateTime updateTime = LocalDateTime.now();
}