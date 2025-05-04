package com.frontend.service;

import com.frontend.entity.store.GlobalPricingOverride;
import com.frontend.entity.store.GlobalTimeSlot;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.repo.GlobalPricingOverrideRepository;
import com.frontend.req.store.GlobalPricingOverrideReq;
import com.frontend.repo.StorePricingScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GlobalPricingOverrideService {

    private final GlobalPricingOverrideRepository repository;
    private final StorePricingScheduleRepository storePricingScheduleRepository; // 用于获取店铺的营业时间

    public GlobalPricingOverride create(GlobalPricingOverrideReq req) {
        GlobalPricingOverride override = new GlobalPricingOverride();
        override.setName(req.getName());
        override.setStartDate(req.getStartDate());  // 设置开始日期
        override.setEndDate(req.getEndDate());      // 设置结束日期
        override.setRegularRate(req.getRegularRate());
        override.setDiscountRate(req.getDiscountRate());

        // 获取店铺的营业时间
        StorePricingSchedule storeSchedule = storePricingScheduleRepository.findByStoreId(req.getStoreId())
                .stream()
                .findFirst() // 假设一个店铺只有一个定价规则
                .orElseThrow(() -> new RuntimeException("Store pricing schedule not found"));
        if (storeSchedule == null) {
            throw new RuntimeException("Store not found or schedule not set");
        }

        List<GlobalTimeSlot> slots = (req.getTimeSlots() != null && !req.getTimeSlots().isEmpty()) ?
                req.getTimeSlots().stream().map(slotReq -> {
                    GlobalTimeSlot slot = new GlobalTimeSlot();
                    slot.setStartTime(slotReq.getStartTime());
                    slot.setEndTime(slotReq.getEndTime());
                    slot.setIsDiscount(slotReq.getIsDiscount());
                    slot.setGlobalOverride(override);
                    return slot;
                }).collect(Collectors.toList()) :
                // 如果没有优惠时段，生成该店铺的常规定价时段
                createStoreRegularSlots(storeSchedule, override);

        // 如果有提供优惠时段，补充其他时段的常规定价
        if (req.getTimeSlots() != null && !req.getTimeSlots().isEmpty()) {
            addRegularSlotsOutsideDiscountTime(slots, storeSchedule, override);
        }

        override.setTimeSlots(slots);
        return repository.save(override);
    }

    public List<GlobalPricingOverride> findAll() {
        return repository.findAll();
    }

    public GlobalPricingOverride findById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Not Found"));
    }

    public GlobalPricingOverride update(Long id, GlobalPricingOverrideReq req) {
        GlobalPricingOverride override = findById(id);
        override.setName(req.getName());
        override.setStartDate(req.getStartDate());  // 设置开始日期
        override.setEndDate(req.getEndDate());      // 设置结束日期
        override.setRegularRate(req.getRegularRate());
        override.setDiscountRate(req.getDiscountRate());

        override.getTimeSlots().clear();
        List<GlobalTimeSlot> newSlots = req.getTimeSlots().stream().map(slotReq -> {
            GlobalTimeSlot slot = new GlobalTimeSlot();
            slot.setStartTime(slotReq.getStartTime());
            slot.setEndTime(slotReq.getEndTime());
            slot.setIsDiscount(slotReq.getIsDiscount());
            slot.setGlobalOverride(override);
            return slot;
        }).collect(Collectors.toList());
        override.getTimeSlots().addAll(newSlots);

        return repository.save(override);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    // 生成店铺的常规定价时段
    private List<GlobalTimeSlot> createStoreRegularSlots(StorePricingSchedule storeSchedule, GlobalPricingOverride override) {
        List<GlobalTimeSlot> regularSlots = new ArrayList<>();

        // 店铺的开店时间和关店时间
        int openHour = storeSchedule.getOpenTime().getHour();
        int closeHour = storeSchedule.getCloseTime().getHour();

        // 创建常规定价的时段
        for (int i = openHour; i < closeHour; i++) {
            GlobalTimeSlot slot = new GlobalTimeSlot();
            slot.setStartTime(LocalTime.parse(String.format("%02d:00", i)));  // 时间段，例如 09:00 - 10:00
            slot.setEndTime(LocalTime.parse(String.format("%02d:00", (i + 1) % 24))); // 下一小时
            slot.setIsDiscount(false); // 常规定价
            slot.setGlobalOverride(override);
            regularSlots.add(slot);
        }
        return regularSlots;
    }

    // 如果有优惠时段，补充那些优惠时段以外的常规定价时段
    private void addRegularSlotsOutsideDiscountTime(List<GlobalTimeSlot> slots, StorePricingSchedule storeSchedule, GlobalPricingOverride override) {
        List<GlobalTimeSlot> regularSlots = createStoreRegularSlots(storeSchedule, override);

        // 过滤出已经在优惠时段中的时间段
        List<String> discountTimeSlots = slots.stream()
                .map(slot -> slot.getStartTime() + "-" + slot.getEndTime())
                .collect(Collectors.toList());

        // 遍历所有常规定价的时段，剔除已包含的优惠时段
        for (GlobalTimeSlot regularSlot : regularSlots) {
            String timeRange = regularSlot.getStartTime() + "-" + regularSlot.getEndTime();
            if (!discountTimeSlots.contains(timeRange)) {
                slots.add(regularSlot); // 添加未包含优惠的常规定价时段
            }
        }
    }
}
