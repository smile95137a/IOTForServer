package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.res.store.StoreRes;
import com.frontend.service.TransactionRecordService;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.frontend.config.security.*;


import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionRecordController {

    @Autowired
    private TransactionRecordService transactionRecordService;

    // 透過 userId 查詢交易紀錄
    @GetMapping("/user")
    public ResponseEntity<ApiResponse<List<TransactionRecord>>> getTransactionsByUser() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        List<TransactionRecord> transactions = transactionRecordService.getTransactionsByUserId(id);
        return ResponseEntity.ok(ResponseUtils.success(transactions));
    }
}
