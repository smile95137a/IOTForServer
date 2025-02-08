package com.frontend.service;

import com.frontend.entity.transection.TransactionRecord;
import com.frontend.repo.TransactionRecordRepository;
import com.frontend.repo.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TransactionRecordService {

    private final TransactionRecordRepository transactionRecordRepository;
    private final UserRepository userRepository;

    public TransactionRecordService(TransactionRecordRepository transactionRecordRepository, UserRepository userRepository) {
        this.transactionRecordRepository = transactionRecordRepository;
        this.userRepository = userRepository;
    }

    // 透過 userId 查詢交易紀錄
    public List<TransactionRecord> getTransactionsByUserId(Long userId) {
        return transactionRecordRepository.findByUserId(userId);
    }
}
