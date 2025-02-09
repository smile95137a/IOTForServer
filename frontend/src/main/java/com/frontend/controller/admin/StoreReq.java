package com.frontend.controller.admin;

import java.util.Set;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.vendor.Vendor;

import com.frontend.req.store.StorePricingScheduleReq;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StoreReq {
    private String name;
    private String address;
    private Vendor vendor;
    private Set<PoolTable> poolTables;
    private Set<StoreEquipment> equipments;
    private Long lat;
    private Long lon;
    private Integer regularRate;
    private Integer discountRate;
    private Integer deposit;
    private Set<StorePricingScheduleReq> pricingSchedules;
}
