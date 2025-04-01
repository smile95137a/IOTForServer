package com.frontend.controller.admin;


import com.frontend.config.message.ApiResponse;
import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.repo.TransactionRecordRepository;
import com.frontend.res.transaction.TransactionsRes;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/b/turnover")
public class turnoverController {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;


    @GetMapping
    public ResponseEntity<ApiResponse<TransactionsRes>> getTodayTotalDeposits() {
        TransactionsRes transactionsRes = transactionRecordRepository.getTodayTotalDeposits();
        return ResponseEntity.ok(ResponseUtils.success(200, null, transactionsRes));
    }



}
