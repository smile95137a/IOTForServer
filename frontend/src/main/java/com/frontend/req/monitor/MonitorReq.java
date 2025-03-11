package com.frontend.req.monitor;

import lombok.Data;

@Data
public class MonitorReq {

    private String name; // 監視器名稱
    
    private Long storeId; // 綁定的 Store ID
}
