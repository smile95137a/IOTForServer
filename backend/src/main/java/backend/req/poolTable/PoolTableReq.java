package backend.req.poolTable;

import backend.entity.poolTable.TableEquipment;
import backend.entity.store.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoolTableReq {
    private String tableNumber;
    private String status;
    private Store store;
    private Set<TableEquipment> tableEquipments;
    private Boolean isUse;
}
