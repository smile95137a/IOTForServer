package com.frontend.res.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createdAt = LocalDateTime.now(); // 創建時間
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime transactionDate; // 交易日期

    private String transactionType; // 交易類型（如 "儲值", "購買", "退款"）

    private User user; // 關聯到 User 表
}