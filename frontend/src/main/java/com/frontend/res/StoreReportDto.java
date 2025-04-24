package com.frontend.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StoreReportDto {
    private BigDecimal todayTotalAmount;      // 当日营业额
    private Integer todayTransactionCount;    // 当日交易笔数
    private BigDecimal todayTopupAmount;      // 当日充值金额
    private Integer todayTopupCount;          // 当日充值笔数
    private List<HourlyRevenueDto> hourlyRevenues;    // 各时段营业额
}



