package com.frontend.controller.admin;

import com.frontend.entity.store.GlobalPricingOverride;
import com.frontend.req.store.GlobalPricingOverrideReq;
import com.frontend.service.GlobalPricingOverrideService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/global-overrides")
@RequiredArgsConstructor
public class GlobalPricingOverrideController {

    private final GlobalPricingOverrideService service;

    @PostMapping
    public GlobalPricingOverride create(@RequestBody GlobalPricingOverrideReq req) {
        return service.create(req);
    }

    @GetMapping
    public List<GlobalPricingOverride> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public GlobalPricingOverride get(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public GlobalPricingOverride update(@PathVariable Long id, @RequestBody GlobalPricingOverrideReq req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
