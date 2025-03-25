package com.frontend.res.game;

import com.frontend.entity.game.GameRecord;

public class GameRes {
    private GameRecord gameRecord;
    private String message;
    private long endTimeMinutes;

    // Getters and Setters
    public GameRecord getGameRecord() {
        return gameRecord;
    }

    public void setGameRecord(GameRecord gameRecord) {
        this.gameRecord = gameRecord;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getEndTimeMinutes() {
        return endTimeMinutes;
    }

    public void setEndTimeMinutes(long endTimeMinutes) {
        this.endTimeMinutes = endTimeMinutes;
    }

    // 构造函数
    public GameRes(GameRecord gameRecord, String message, long endTimeMinutes) {
        this.gameRecord = gameRecord;
        this.message = message;
        this.endTimeMinutes = endTimeMinutes;
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
