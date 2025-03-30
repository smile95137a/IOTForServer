package com.frontend.res.store;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StoreEquipment;
import com.frontend.entity.vendor.Vendor;
import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoreRes {

    private Long id;
    private String uid;
    private String name;
    private String address;
    private Vendor vendor;
    private String imgUrl;
    private Set<StoreEquipment> equipments;
    private Set<PoolTable> poolTables;
    private Set<StorePricingScheduleRes> pricingSchedules;
    private String lat;
    private String lon;
    private Integer deposit;
    private String hint;
    private String contactPhone;
    private Integer bookTime;
    private Integer cancelBookTime;
}
