package src.main.java.com.frontend.controller;

import src.main.java.com.frontend.config.message.ApiResponse;
import src.main.java.com.frontend.repo.UserRepository;
import src.main.java.com.frontend.utils.ResponseUtils;
import src.main.java.com.frontend.utils.SecurityUtils;
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

    @PostMapping("/topOp") //儲值
    public ResponseEntity<ApiResponse<?>> topOp() throws Exception {
        var userDetails = SecurityUtils.getSecurityUser();
        var userId = userDetails.getId();
        try {
            ApiResponse<Object> success = ResponseUtils.success(200, null, null);
            return ResponseEntity.ok(success);
        } catch (Exception e) {
            e.printStackTrace();
            ApiResponse<Object> error = ResponseUtils.error(999,  e.getMessage(), null);
            return ResponseEntity.ok(error);
        }
    }
}
