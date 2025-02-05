package com.frontend.entity.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class GameOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 訂單ID

    @Column(nullable = false)
    private String userId; // 用戶ID，假設是字串

    @Column(nullable = false)
    private String gameId; // 遊戲ID

    @Column(nullable = false)
    private double totalPrice; // 總價格

    @Column(nullable = false)
    private LocalDateTime startTime; // 遊戲開始時間

    @Column(nullable = false)
    private LocalDateTime endTime; // 遊戲結束時間

    @Column(nullable = false)
    private long duration; // 遊玩時長（小時）
}
