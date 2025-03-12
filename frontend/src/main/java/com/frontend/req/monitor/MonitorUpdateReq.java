package com.frontend.req.monitor;

import com.frontend.entity.store.Store;
import lombok.Data;

@Data
public class MonitorUpdateReq {
    private String uid;
    private Boolean status; // 監視器狀態
    private String name;
    private Long storeId;
}
