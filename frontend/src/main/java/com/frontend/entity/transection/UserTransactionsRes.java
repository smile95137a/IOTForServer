package com.frontend.entity.transection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class UserTransactionsRes {

    private BigDecimal totalAmount;

    private Integer count;
}
