package com.frontend.res.transaction;

import com.frontend.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRecordRes {

    private Long id;

    private Integer amount; // 消費金額

    private LocalDateTime createdAt = LocalDateTime.now(); // 創建時間

    private LocalDateTime transactionDate; // 交易日期

    private String transactionType; // 交易類型（如 "儲值", "購買", "退款"）

    private User user; // 關聯到 User 表
}