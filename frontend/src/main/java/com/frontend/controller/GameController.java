package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.game.GameRecord;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameResponse;
import com.frontend.service.GameService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/game")
public class GameController {

    @Autowired
    private GameService gameService;

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<?>> startGame(@RequestBody GameReq gameReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            GameRecord gameRecord = gameService.startGame(gameReq , id);
            return ResponseEntity.ok(ResponseUtils.success(200, "開台成功", gameRecord));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    @PostMapping("/end")
    public ResponseEntity<ApiResponse<?>> endGame(@RequestBody GameReq gameReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            GameResponse gameResponse = gameService.endGame(gameReq, id);
            return ResponseEntity.ok(ResponseUtils.success(200, "開台成功", gameResponse));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


    }


    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(@RequestBody CheckoutReq checkoutReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            gameService.checkout(checkoutReq , id);
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


        return ResponseEntity.ok(null);
    }

}
