package com.frontend.res;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HourlyRevenueDto {
    private Integer hour;            // 小时（0-23）
    private Integer orderCount;      // 订单数量
    private BigDecimal revenue;      // 营业额
}