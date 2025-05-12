package com.frontend.res.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GamePriceRes {

    private Long second; //時長

    private Integer deposit; //押金

    private Long totalRawMinutes; //總共遊玩時間

    private Long totalDiscountMinutes; //總優惠時段

    private Long totalRegularMinutes; //總一般時段

    private double discountPrice; //優惠金額

    private double regularPrice; //一班金額

    private double totalPrice; //總金額
}
