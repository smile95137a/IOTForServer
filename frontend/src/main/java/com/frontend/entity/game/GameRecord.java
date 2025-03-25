package com.frontend.entity.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

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
    @JsonFormat(pattern = "yyyy/MM/dd HH.mm.ss")
    private LocalDateTime startTime; // 遊戲開始時間戳
    @Column
    private String userUid; // 用戶UID
    @Column
    private int price;      // 押金金額
    @Column
    private String status;  // 狀態 (開始 / 結束等)
    @Column
    private Long storeId;
    @Column
    private String storeName;
    @Column
    private Long vendorId;
    @Column
    private String vendorName;
    @Column
    private String contactInfo;
    @Column
    private Long poolTableId;
    @Column
    private String poolTableName;

    // 新增字段
    @Column
    private Integer regularRateAmount; // 一班时段的金额
    @Column
    private Integer discountRateAmount; // 优惠时段的金额
}
