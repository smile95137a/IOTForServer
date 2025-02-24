package com.frontend.res.user;

import java.math.BigDecimal;

public class UserRemainingBalanceDTO {

    private Long userId;
    private String userName;
    private BigDecimal remainingBalance;

    public UserRemainingBalanceDTO(Long userId, String userName, BigDecimal remainingBalance) {
        this.userId = userId;
        this.userName = userName;
        this.remainingBalance = remainingBalance;
    }

    // Getters and setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public BigDecimal getRemainingBalance() {
        return remainingBalance;
    }

    public void setRemainingBalance(BigDecimal remainingBalance) {
        this.remainingBalance = remainingBalance;
    }
}
