package backend.req.vendor;

import backend.entity.store.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendorReq {

    private String name;
    private String contactInfo;
    private Set<Store> store;
}
