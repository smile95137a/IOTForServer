package com.frontend.entity.transection;

import com.frontend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Entity
@Table(name = "game_transaction_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameTransactionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String uid;

    @Column(nullable = false)
    private Integer amount; // 消費金額

    @Column
    private String storeName;

    @Column
    private String tableNumber;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now(); // 創建時間

    @Column(nullable = false)
    private LocalDateTime transactionDate; // 交易日期

    @Column(nullable = false, length = 50)
    private String transactionType; // 交易類型（如 "儲值", "購買", "退款"）

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 關聯到 User 表
}
