package com.frontend.service;

import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.repo.RechargeStandardRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RechargeStandardService {
    private final RechargeStandardRepository repository;

    public RechargeStandardService(RechargeStandardRepository repository) {
        this.repository = repository;
    }

    public List<RechargeStandard> findAll() {
        return repository.findAll();
    }

    public RechargeStandard findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public RechargeStandard save(RechargeStandard standard) {
        return repository.save(standard);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}