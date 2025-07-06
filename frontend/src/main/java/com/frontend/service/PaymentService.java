package com.frontend.service;

import com.frontend.entity.topLog.SendLog;
import com.frontend.entity.topLog.TopLog;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.SendLogRepository;
import com.frontend.repo.TopLogRepository;
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

    @Autowired
    private TopLogRepository topLogRepository;

    @Autowired
    private SendLogRepository sendLogRepository;

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

        if(topOpReq.getIsFirst()){
            TopLog topLog = new TopLog();
            topLog.setIsFirst(true);
            topLog.setUserDate(LocalDateTime.now());
            topLog.setUserId(userId);

            topLogRepository.save(topLog);
        }

        return user.getAmount();
    }

    public Boolean getUserUse(Long userId) {
        TopLog byUserId = topLogRepository.findByUserId(userId);

        if(byUserId == null){
            return false;
        }

        return byUserId.getIsFirst();
    }

    public Boolean getSendUse(Long userId) {
        SendLog byUserId = sendLogRepository.findByUserId(userId);

        if(byUserId == null){
            return false;
        }

        return byUserId.getIsSend();
    }

    public Integer sendTop(TopOpReq topOpReq , Long userId){
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
        transactionRecord.setTransactionType("SEND");
        transactionRecord.setPayType(topOpReq.getPayType());
        transactionRecord.setUser(user);
        transactionRecordRepository.save(transactionRecord);

        if(topOpReq.getIsFirst()){
            SendLog sendLog = new SendLog();
            sendLog.setIsSend(true);
            sendLog.setUserDate(LocalDateTime.now());
            sendLog.setUserId(userId);

            sendLogRepository.save(sendLog);
        }

        return user.getAmount();
    }
}
