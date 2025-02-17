package com.frontend.res.store;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.store.StorePricingSchedule;
import com.frontend.entity.vendor.Vendor;
import jakarta.persistence.Column;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminStoreRes {
    private Long id;
    private String uid;
    private String name;
    private String address;
    private Vendor vendor;
    private String imgUrl;
    private Set<StoreEquipment> equipments;
    private Set<PoolTable> poolTables;
    private Set<StorePricingSchedule> pricingSchedules;
    private String lat;
    private String lon;
    private Integer deposit;
}
