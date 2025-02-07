package com.frontend.entity.game;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String gameId;  // 唯一的遊戲識別碼 (UUID)
    @Column
    private LocalDateTime startTime; // 遊戲開始時間戳
    @Column
    private String userUid; // 用戶UID
    @Column
    private int price;      // 押金金額
    @Column
    private String status;  // 狀態 (開始 / 結束等)
}
