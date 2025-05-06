package com.frontend.service;

import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.repo.RechargeStandardRepository;
import com.frontend.req.store.RechargeStandardReq;
import com.frontend.res.store.RechargeStandardRes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RechargeStandardService {

    private final RechargeStandardRepository repository;

    public List<RechargeStandardRes> findAll() {
        return repository.findAll().stream()
                .map(this::toRes)
                .collect(Collectors.toList());
    }

    public RechargeStandardRes findById(Long id) {
        RechargeStandard entity = repository.findById(id).orElse(null);
        return toRes(entity);
    }

    public RechargeStandardRes create(RechargeStandardReq req) {
        RechargeStandard entity = toEntity(req);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return toRes(repository.save(entity));
    }

    public RechargeStandardRes update(Long id, RechargeStandardReq req) {
        RechargeStandard entity = toEntity(req);
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        return toRes(repository.save(entity));
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private RechargeStandardRes toRes(RechargeStandard entity) {
        if (entity == null) return null;
        RechargeStandardRes res = new RechargeStandardRes();
        res.setId(entity.getId());
        res.setRechargeAmount(entity.getRechargeAmount());
        res.setBonusAmount(entity.getBonusAmount());
        res.setStatus(entity.getStatus());
        res.setCreateTime(entity.getCreateTime());
        res.setUpdateTime(entity.getUpdateTime());
        return res;
    }

    private RechargeStandard toEntity(RechargeStandardReq req) {
        RechargeStandard entity = new RechargeStandard();
        entity.setRechargeAmount(req.getRechargeAmount());
        entity.setBonusAmount(req.getBonusAmount());
        entity.setStatus(req.getStatus());
        return entity;
    }
}
