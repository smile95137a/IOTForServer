package com.frontend.controller.admin;

import java.util.Set;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.vendor.Vendor;

import com.frontend.req.store.StorePricingScheduleReq;
import jakarta.persistence.Column;
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
    private String lat;
    private String lon;
    private Integer regularRate;
    private Integer discountRate;
    private Integer deposit;
    private String imgUrl;
    private Set<StorePricingScheduleReq> pricingSchedules;
    private String hint;
    private String contactPhone;
}
