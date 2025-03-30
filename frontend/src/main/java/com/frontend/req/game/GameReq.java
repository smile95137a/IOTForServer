package com.frontend.req.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReq {
    private String gameId;

    private String poolTableUId;

    private Long poolTableId;
}
