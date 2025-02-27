package com.frontend.res.report;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionSummary {
    private String period;
    private BigDecimal totalAmount;
    
    // 构造函数、getter和setter
    public TransactionSummary(String period, BigDecimal totalAmount) {
        this.period = period;
        this.totalAmount = totalAmount;
    }
    
    // getter和setter方法
}