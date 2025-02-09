package com.frontend.res.store;

import com.frontend.entity.poolTable.PoolTable;
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
    private Set<PoolTable> poolTables;
}
