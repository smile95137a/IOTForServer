package com.model.enums;

import java.util.Random;

public enum PoolTableStatus {
    IN_USE, // 正在使用中（有人正在打球）;
    AVAILABLE, // 可用状态（无人使用，可供预订或直接使用）;
    RESERVED, // 已预订，但尚未使用;
    UNDER_MAINTENANCE, // 维护中（不可用，例如维修或保养）;
    OUT_OF_ORDER, // 故障（需要修理）;
    CLEANING, // 清洁中（暂时不可用）;
    CLOSED; // 关闭状态（营业时间外或不可用）;

    public static String initGetRandomPoolTableStatus() {
        PoolTableStatus[] statuses = PoolTableStatus.values();
        int randomIndex = new Random().nextInt(statuses.length);
        return statuses[randomIndex].name();
    }
}
