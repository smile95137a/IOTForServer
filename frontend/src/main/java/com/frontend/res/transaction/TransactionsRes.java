package com.frontend.res.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsRes {
    private BigDecimal todayTotalAmount;         // 今日总金额
    private Integer todayTransactionCount;       // 今日交易笔数
    private BigDecimal todayTopupAmount;         // 今日充值金额
    private Integer todayTopupCount;             // 今日充值笔数
    private BigDecimal monthTotalAmount;         // 本月总金额
    private Integer monthTransactionCount;       // 本月交易笔数
    private Long storeId;                        // 店铺 ID

    public TransactionsRes(BigDecimal amount, Integer count, Long storeId) {
        this.todayTotalAmount = amount;
        this.todayTransactionCount = count;
        this.storeId = storeId;
    }

    public TransactionsRes(BigDecimal amount, Integer count) {
        this.todayTotalAmount = amount;
        this.todayTransactionCount = count;
    }
}

