package com.frontend.pattern.game;

import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameRes;

public interface GameStartStrategy {
    GameRes startGame(GameReq gameReq, Long userId) throws Exception;
}
