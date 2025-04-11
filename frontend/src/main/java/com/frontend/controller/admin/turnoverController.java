package com.frontend.controller.admin;


import com.frontend.config.message.ApiResponse;
import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.repo.GameOrderRepository;
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


    @Autowired
    private GameOrderRepository gameOrderRepository;


    @GetMapping
    public ResponseEntity<ApiResponse<TransactionsRes>> getTransactionsSummary() {
        // 获取今天的储值总金额和笔数
        TransactionsRes todayDeposits = transactionRecordRepository.getTodayTotalDeposits();

        // 获取今天的消费总金额和笔数
        TransactionsRes todayConsumption = gameOrderRepository.getTodayTotalConsumption();

        // 获取本月的总消费金额和笔数
        TransactionsRes monthConsumption = gameOrderRepository.getMonthTotalConsumption();

        // 创建一个汇总对象，适配前端需要的字段名
        TransactionsRes summaryRes = new TransactionsRes();

        // 设置今日充值数据
        summaryRes.setTodayTopupAmount(todayDeposits.getTodayTotalAmount());
        summaryRes.setTodayTopupCount(todayDeposits.getTodayTransactionCount());

        // 设置今日消费数据
        summaryRes.setTodayTotalAmount(todayConsumption.getTodayTotalAmount());
        summaryRes.setTodayTransactionCount(todayConsumption.getTodayTransactionCount());

        // 设置本月消费数据
        summaryRes.setMonthTotalAmount(monthConsumption.getTodayTotalAmount());
        summaryRes.setMonthTransactionCount(monthConsumption.getTodayTransactionCount());

        // 返回响应
        return ResponseEntity.ok(ResponseUtils.success(200, null, summaryRes));
    }





}
