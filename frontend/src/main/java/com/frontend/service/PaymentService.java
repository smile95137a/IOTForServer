package com.frontend.service;

import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.TransactionRecordRepository;
import com.frontend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    public Integer topOp(Integer price , Long userId){
        User user = userRepository.findById(userId).get();
        Integer newPrice = user.getAmount() + price;
        user.setAmount(newPrice);
        userRepository.save(user);

        //儲值紀錄
        TransactionRecord transactionRecord = new TransactionRecord();
        transactionRecord.setTransactionDate(LocalDateTime.now());
        transactionRecord.setCreatedAt(LocalDateTime.now());
        transactionRecord.setAmount(price);
        transactionRecord.setTransactionType("DEPOSIT");
        transactionRecord.setUser(user);
        transactionRecordRepository.save(transactionRecord);



        return user.getAmount();
    }
}
