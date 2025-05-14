package com.frontend.res.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.vendor.Vendor;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameRecordRes {

    private Long id;

    private String gameId;  // 唯一的遊戲識別碼 (UUID)
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime startTime; // 遊戲開始時間戳
    private String userUid; // 用戶UID
    private int price;      // 押金金額
    private String status;  // 狀態 (開始 / 結束等)
    private Long storeId;
    private String storeName;
    private Long vendorId;
    private String vendorName;
    private String contactInfo;
    private String storePhone;
    private Long poolTableId;
    private String poolTableName;

    // 新增字段
    private Double regularRateAmount; // 一班时段的金额
    private Double discountRateAmount; // 优惠时段的金额

    private String hint;

    private Vendor vendor;
}
