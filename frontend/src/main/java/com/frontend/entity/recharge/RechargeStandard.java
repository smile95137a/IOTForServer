package com.frontend.entity.recharge;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import com.frontend.enums.PromotionStatus;
@Entity
@Table(name = "recharge_standard")
@Data
public class RechargeStandard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rechargeAmount;

    @Column(nullable = false)
    private Integer bonusAmount;

    @Enumerated(EnumType.STRING)
    private PromotionStatus status;

    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime = LocalDateTime.now();
    @Column
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime = LocalDateTime.now();
}