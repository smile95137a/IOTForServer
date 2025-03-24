package com.frontend.res.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameOrderRes {
    private String gameId;
    private Integer totalPrice;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long duration;
    private String status;
}
