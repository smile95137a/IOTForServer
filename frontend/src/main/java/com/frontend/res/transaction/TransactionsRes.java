package com.frontend.res.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsRes {

    private BigDecimal todayTotalAmount;

    private Integer todayTransactionCount;
}
