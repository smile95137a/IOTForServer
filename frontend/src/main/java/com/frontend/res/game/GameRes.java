package com.frontend.res.game;

import com.frontend.entity.game.GameRecord;
import com.frontend.entity.vendor.Vendor;
import lombok.Data;

@Data
public class GameRes {
    private GameRecord gameRecord;
    private String message;
    private long endTimeMinutes;
    private String storePhone;
    private Vendor vendor;

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
