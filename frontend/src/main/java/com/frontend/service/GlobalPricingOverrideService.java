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
    private final StorePricingScheduleRepository storePricingScheduleRepository;

    public GlobalPricingOverride create(GlobalPricingOverrideReq req) {
        GlobalPricingOverride override = new GlobalPricingOverride();
        override.setName(req.getName());
        override.setStartDate(req.getStartDate());
        override.setEndDate(req.getEndDate());
        override.setRegularRate(req.getRegularRate());
        override.setDiscountRate(req.getDiscountRate());

        StorePricingSchedule storeSchedule = storePricingScheduleRepository.findByStoreId(req.getStoreId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Store pricing schedule not found"));

        List<GlobalTimeSlot> slots = (req.getTimeSlots() != null && !req.getTimeSlots().isEmpty()) ?
                req.getTimeSlots().stream().map(slotReq -> {
                    GlobalTimeSlot slot = new GlobalTimeSlot();
                    slot.setStartTime(slotReq.getStartTime());
                    slot.setEndTime(slotReq.getEndTime());
                    slot.setIsDiscount(slotReq.getIsDiscount());
                    slot.setGlobalOverride(override);
                    return slot;
                }).collect(Collectors.toList()) :
                createStoreRegularSlots(storeSchedule, override);

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
        override.setStartDate(req.getStartDate());
        override.setEndDate(req.getEndDate());
        override.setRegularRate(req.getRegularRate());
        override.setDiscountRate(req.getDiscountRate());

        // 清除舊的 slot
        override.getTimeSlots().clear();

        StorePricingSchedule storeSchedule = storePricingScheduleRepository.findByStoreId(req.getStoreId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Store pricing schedule not found"));

        List<GlobalTimeSlot> slots = (req.getTimeSlots() != null && !req.getTimeSlots().isEmpty()) ?
                req.getTimeSlots().stream().map(slotReq -> {
                    GlobalTimeSlot slot = new GlobalTimeSlot();
                    slot.setStartTime(slotReq.getStartTime());
                    slot.setEndTime(slotReq.getEndTime());
                    slot.setIsDiscount(slotReq.getIsDiscount());
                    slot.setGlobalOverride(override);
                    return slot;
                }).collect(Collectors.toList()) :
                createStoreRegularSlots(storeSchedule, override);

        if (req.getTimeSlots() != null && !req.getTimeSlots().isEmpty()) {
            addRegularSlotsOutsideDiscountTime(slots, storeSchedule, override);
        }

        override.getTimeSlots().addAll(slots);

        return repository.save(override);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    private List<GlobalTimeSlot> createStoreRegularSlots(StorePricingSchedule storeSchedule, GlobalPricingOverride override) {
        List<GlobalTimeSlot> regularSlots = new ArrayList<>();

        int openHour = storeSchedule.getOpenTime().getHour();
        int closeHour = storeSchedule.getCloseTime().getHour();

        for (int i = openHour; i < closeHour; i++) {
            GlobalTimeSlot slot = new GlobalTimeSlot();
            slot.setStartTime(LocalTime.of(i, 0));
            slot.setEndTime(LocalTime.of((i + 1) % 24, 0));
            slot.setIsDiscount(false);
            slot.setGlobalOverride(override);
            regularSlots.add(slot);
        }

        return regularSlots;
    }

    private void addRegularSlotsOutsideDiscountTime(List<GlobalTimeSlot> slots, StorePricingSchedule storeSchedule, GlobalPricingOverride override) {
        List<GlobalTimeSlot> regularSlots = createStoreRegularSlots(storeSchedule, override);

        List<String> discountTimeSlots = slots.stream()
                .map(slot -> slot.getStartTime() + "-" + slot.getEndTime())
                .collect(Collectors.toList());

        for (GlobalTimeSlot regularSlot : regularSlots) {
            String timeRange = regularSlot.getStartTime() + "-" + regularSlot.getEndTime();
            if (!discountTimeSlots.contains(timeRange)) {
                slots.add(regularSlot);
            }
        }
    }
}
