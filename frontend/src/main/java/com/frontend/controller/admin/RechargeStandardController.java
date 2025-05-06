package com.frontend.controller.admin;

import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.req.store.RechargeStandardReq;
import com.frontend.res.store.RechargeStandardRes;
import com.frontend.service.RechargeStandardService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/b/recharge-standards")
public class RechargeStandardController {
    private final RechargeStandardService service;

    public RechargeStandardController(RechargeStandardService service) {
        this.service = service;
    }

    @GetMapping
    public List<RechargeStandardRes> list() {
        return service.findAll().stream().map(this::toRes).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public RechargeStandardRes get(@PathVariable Long id) {
        return toRes(service.findById(id));
    }

    @PostMapping
    public RechargeStandardRes create(@RequestBody RechargeStandardReq req) {
        RechargeStandard entity = toEntity(req);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return toRes(service.save(entity));
    }

    @PutMapping("/{id}")
    public RechargeStandardRes update(@PathVariable Long id, @RequestBody RechargeStandardReq req) {
        RechargeStandard entity = toEntity(req);
        entity.setId(id);
        entity.setUpdateTime(LocalDateTime.now());
        return toRes(service.save(entity));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    // ---------- Mapping Methods ----------

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
