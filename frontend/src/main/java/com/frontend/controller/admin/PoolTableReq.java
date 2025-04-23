package com.frontend.controller.admin;

import java.util.Set;

import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.Store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoolTableReq {
    private String tableUId;
    private String tableNumber;
    private String status;
    private Store store;
    private Set<TableEquipment> tableEquipments;
    private Boolean isUse;
}
