package com.frontend.service;

import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.TransactionRecordRepository;
import com.frontend.repo.UserRepository;
import com.frontend.req.topOp.TopOpReq;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    public Integer topOp(TopOpReq topOpReq , Long userId){
        User user = userRepository.findById(userId).get();
        Integer newPrice = user.getAmount() + topOpReq.getPrice();
        Integer newPoint = user.getPoint() + topOpReq.getPoint();
        Integer newBalance = newPrice + newPoint;
        user.setAmount(newPrice);
        user.setPoint(newPoint);
        user.setBalance(newBalance);
        userRepository.save(user);

        //儲值紀錄
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setTransactionDate(LocalDateTime.now());
        transactionRecord.setCreatedAt(LocalDateTime.now());
        transactionRecord.setAmount(topOpReq.getPrice());
        transactionRecord.setTransactionType("DEPOSIT");
        transactionRecord.setPayType(topOpReq.getPayType());
        transactionRecord.setUser(user);
        transactionRecordRepository.save(transactionRecord);



        return user.getAmount();
    }
}
