package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.game.GameRecord;
import com.frontend.req.game.BookGameReq;
import com.frontend.req.game.CheckoutReq;
import com.frontend.req.game.GameReq;
import com.frontend.res.game.GameRes;
import com.frontend.res.game.GameResponse;
import com.frontend.service.GameService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;


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
            GameRes gameRecord = gameService.startGame(gameReq , id);
            return ResponseEntity.ok(ResponseUtils.success(200, "開台成功", gameRecord));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.success(9999, e.getMessage(), false));
        }
    }

//    @PostMapping("/end")
//    public ResponseEntity<ApiResponse<?>> endGame(@RequestBody GameReq gameReq) {
//        try {
//            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
//            Long id = securityUser.getId();
//            GameResponse gameResponse = gameService.endGame(gameReq, id);
//            return ResponseEntity.ok(ResponseUtils.success(200, "開台成功", gameResponse));
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
//        }
//
//
//    }


    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(@RequestBody CheckoutReq checkoutReq) {
        try {
            UserPrinciple securityUser = SecurityUtils.getSecurityUser();
            Long id = securityUser.getId();
            gameService.checkout(checkoutReq , id);
            return ResponseEntity.ok(ResponseUtils.success(true));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


    }
    @GetMapping("/available-times")
    public ResponseEntity<ApiResponse<?>> getAvailableTimes(
            @RequestParam Long storeId,
            @RequestParam String bookingDate,
            @RequestParam Long poolTableId
    ) {
        try {
            LocalDate date = LocalDate.parse(bookingDate);
            Map<String, List<Map<String, Object>>> availableTimes = gameService.getAvailableTimes(storeId, date , poolTableId);
            return ResponseEntity.ok(ResponseUtils.success(availableTimes));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    /**
     * 預約遊戲
     *
     * @param gameReq 預約請求
     * @return 預約成功的 GameRecord
     * @throws Exception 當有錯誤發生時
     */
    @PostMapping("/book")
    public ResponseEntity<ApiResponse<?>> bookGame(@RequestBody BookGameReq gameReq) {
        try {
            GameRecord gameRecord = gameService.bookGame(gameReq);
            return ResponseEntity.status(HttpStatus.CREATED).body(ResponseUtils.success(gameRecord));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

    /**
     * 取消預約
     *
     * @param gameReq 取消預約請求
     * @return 註明是否取消成功
     * @throws Exception 當有錯誤發生時
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<?>> cancelBook(@RequestBody GameReq gameReq) {
        try {
            gameService.cancelBook(gameReq);
            return ResponseEntity.ok(ResponseUtils.success("預約已取消"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtils.error(9999, "取消預約失敗：" + e.getMessage(), null));
        }
    }

    // 啟動遊戲
    @PostMapping("/bookStart")
    public ResponseEntity<ApiResponse<?>> bookStart(@RequestBody GameReq gameReq) {
        try {
            GameRecord gameRecord = gameService.bookStartGame(gameReq);
            return ResponseEntity.ok(ResponseUtils.success(gameRecord));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseUtils.error(9999, e.getMessage(), null));
        }
    }

}
