package com.frontend.req.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReq {

    private Integer price;

    private String userUid;

    private LocalDateTime endTime;

    private String gameId;

    private Integer gamePrice;
}
