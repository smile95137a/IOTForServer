package com.frontend.controller.admin;

import com.frontend.entity.recharge.RechargeStandard;
import com.frontend.service.RechargeStandardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b/recharge-standards")
public class RechargeStandardController {
    private final RechargeStandardService service;

    public RechargeStandardController(RechargeStandardService service) {
        this.service = service;
    }

    @GetMapping
    public List<RechargeStandard> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public RechargeStandard get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public RechargeStandard create(@RequestBody RechargeStandard standard) {
        return service.save(standard);
    }

    @PutMapping("/{id}")
    public RechargeStandard update(@PathVariable Long id, @RequestBody RechargeStandard standard) {
        standard.setId(id);
        return service.save(standard);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
