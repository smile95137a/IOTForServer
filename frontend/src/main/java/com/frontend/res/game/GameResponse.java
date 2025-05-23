package com.frontend.res.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameResponse {
    private long totalSeconds;
    private double totalPrice;
    private String gameId;

}
