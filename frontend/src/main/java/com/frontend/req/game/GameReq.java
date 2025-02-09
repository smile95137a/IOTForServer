package com.frontend.req.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReq {

    private Integer price;

    private String userUid;

    private String gameId;

    private Integer gamePrice;

    private String poolTableUId;
}
