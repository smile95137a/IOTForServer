package com.frontend.enums;

import java.util.Random;

public enum PoolTableStatus {
    AVAILABLE("上架"),       // 奖品上架
    UNAVAILABLE("下架");

    private final String description;

    PoolTableStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
