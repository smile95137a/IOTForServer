package backend.req.store;

import backend.entity.poolTable.PoolTable;
import backend.entity.store.StoreEquipment;
import backend.entity.vendor.Vendor;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

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
    private String regularDateRangeStart;
    private String regularDateRangeEnd;
    private String discountDateRangeStart;
    private String discountDateRangeEnd;
    private String regularTimeRangeStart;
    private String regularTimeRangeEnd;
    private String discountTimeRangeStart;
    private String discountTimeRangeEnd;
}
