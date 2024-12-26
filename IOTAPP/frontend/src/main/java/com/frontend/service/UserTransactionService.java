package com.frontend.service;

import com.frontend.entity.log.UserTransaction;
import com.frontend.repo.UserTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserTransactionService {

    @Autowired
    private UserTransactionRepository userTransactionRepository;

    public List<UserTransaction> getAll(){
        List<UserTransaction> all = userTransactionRepository.findAll();
        if(all.isEmpty()){
            return new ArrayList<>();
        }
        return all;
    }



}
