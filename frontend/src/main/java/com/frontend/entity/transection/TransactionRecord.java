package com.frontend.entity.transection;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transaction_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer amount; // 消費金額

    @Column(nullable = false, updatable = false)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now(); // 創建時間

    @Column(nullable = false)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime transactionDate; // 交易日期

    @Column(nullable = false, length = 50)
    private String transactionType; // 交易類型（如 "儲值", "購買", "退款"）

    @Column(nullable = false, length = 50)
    private String payType; // 交易類型（如 "儲值", "購買", "退款"）

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 關聯到 User 表
}
