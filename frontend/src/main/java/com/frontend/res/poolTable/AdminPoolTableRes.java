package com.frontend.res.poolTable;

import com.frontend.entity.poolTable.TableEquipment;
import com.frontend.entity.store.Store;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminPoolTableRes {
    private String uid;
    private String tableNumber;
    private String status;
    private Set<TableEquipment> tableEquipments;
    private Long storeId;
}
