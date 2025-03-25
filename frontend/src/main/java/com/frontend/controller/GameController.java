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
            return ResponseEntity.ok(ResponseUtils.success(true));
        }catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(ResponseUtils.error(9999, e.getMessage(), null));
        }


    }


    @GetMapping("/available-times")
    public ResponseEntity<List<String>> getAvailableTimes(
            @RequestParam Long storeId,
            @RequestParam String bookingDate,
            @RequestParam int timeSlotHours) {

        LocalDate date = LocalDate.parse(bookingDate);
        List<String> availableTimes = gameService.getAvailableTimes(storeId, date, timeSlotHours);

        return ResponseEntity.ok(availableTimes);
    }


    /**
     * 預約遊戲
     *
     * @param gameReq 預約請求
     * @return 預約成功的 GameRecord
     * @throws Exception 當有錯誤發生時
     */
    @PostMapping("/book")
    public ResponseEntity<GameRecord> bookGame(@RequestBody BookGameReq gameReq) throws Exception {
        try {
            GameRecord gameRecord = gameService.bookGame(gameReq);
            return ResponseEntity.status(HttpStatus.CREATED).body(gameRecord);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
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
    public ResponseEntity<String> cancelBook(@RequestBody GameReq gameReq) throws Exception {
        try {
            gameService.cancelBook(gameReq);
            return ResponseEntity.ok("預約已取消");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("取消預約失敗：" + e.getMessage());
        }
    }

    // 啟動遊戲
    @PostMapping("/bookStart")
    public ResponseEntity<GameRecord> bookStart(@RequestBody GameReq gameReq) {
        try {
            GameRecord gameRecord = gameService.bookStartGame(gameReq);
            return ResponseEntity.ok(gameRecord);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body(null);
        }
    }
}
