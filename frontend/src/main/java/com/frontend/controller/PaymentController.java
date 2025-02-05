package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.repo.UserRepository;
import com.frontend.service.PaymentService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/topOp") //儲值
    public ResponseEntity<ApiResponse<?>> topOp(@RequestBody Integer price) throws Exception {
        var userDetails = SecurityUtils.getSecurityUser();
        var userId = userDetails.getId();
        try {
            Integer newPrice = paymentService.topOp(price, userId);
            ApiResponse<Object> success = ResponseUtils.success(200, String.format("新增金額為%d元" , newPrice), null);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Object> error = ResponseUtils.error(999,  e.getMessage(), null);
            return ResponseEntity.ok(error);
        }
    }
}
