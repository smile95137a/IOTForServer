package com.frontend.res.poolTable;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.StorePricingSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StorePoolTableRes {

    private PoolTable poolTable;  // 桌台信息

    private List<StorePricingSchedule> pricingSchedules;  // 店家优惠时段信息
}
