package com.frontend.res.game;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.vendor.Vendor;
import com.frontend.req.store.TimeSlotInfo;
import lombok.Data;

@Data
public class GameRes {
    private GameRecord gameRecord;
    private String message;
    private long endTimeMinutes;
    private String storePhone;
    private Vendor vendor;

    /**
     * 当前时段信息
     * 用于前端判断显示颜色：
     * - 一般时段：显示橘色
     * - 优惠时段：显示不同颜色
     */
    private TimeSlotInfo currentTimeSlot;

    // 构造函数
    public GameRes(GameRecord gameRecord, String message, long endTimeMinutes , Vendor vendor , String storePhone) {
        this.gameRecord = gameRecord;
        this.message = message;
        this.endTimeMinutes = endTimeMinutes;
        this.vendor = vendor;
        this.storePhone = storePhone;
    }

    @Override
    public String toString() {
        return "GameResponse{" +
                "gameRecord=" + gameRecord +
                ", message='" + message + '\'' +
                ", endTimeMinutes=" + endTimeMinutes +
                '}';
    }
}
