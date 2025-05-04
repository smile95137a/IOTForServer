package com.frontend.repo;

import com.frontend.entity.store.GlobalPricingOverride;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface GlobalPricingOverrideRepository extends JpaRepository<GlobalPricingOverride, Long> {
}