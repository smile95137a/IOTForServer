package com.frontend.service;

import com.frontend.entity.user.User;
import com.frontend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    @Autowired
    private UserRepository userRepository;

    public Integer topOp(Integer price , Long userId){
        User user = userRepository.findById(userId).get();
        Integer newPrice = user.getAmount() + price;
        user.setAmount(newPrice);
        userRepository.save(user);
        return user.getAmount();
    }
}
