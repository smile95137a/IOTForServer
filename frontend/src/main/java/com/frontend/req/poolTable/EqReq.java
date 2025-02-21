package com.frontend.req.poolTable;

import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqReq {

    private String name;
    private LocalTime autoStartTime;
    private LocalTime autoStopTime;
    private String description;

    private PoolTable poolTable;

    private Store store;

}
