package com.frontend.res.monitor;

import lombok.Data;

@Data
public class MonitorRes {


    private String uid;

    private String name;

    private String number;

    private boolean status;

    private Long storeId;

    private String storeName;

    private String storeIP;
}
