package com.frontend.req.poolTable;

import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.store.Store;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EqReq {

    private String name;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Taipei")
    private LocalTime autoStartTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Taipei")
    private LocalTime autoStopTime;
    private String description;

    private PoolTable poolTable;

    private Store store;

}
