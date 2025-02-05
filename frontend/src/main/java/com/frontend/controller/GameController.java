package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.req.game.GameReq;
import com.frontend.res.store.StoreRes;
import com.frontend.service.GameService;
import com.frontend.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<?>> startGame(@RequestBody GameReq gameReq) {
        try {
            gameService.startGame(gameReq);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


        return ResponseEntity.ok(null);
    }

    @PostMapping("/end")
    public ResponseEntity<ApiResponse<?>> endGame(@RequestBody GameReq gameReq) {
        try {
            gameService.endGame(gameReq);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


        return ResponseEntity.ok(null);
    }

}
