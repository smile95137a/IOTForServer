package com.frontend.req.router;

import com.frontend.enums.RouterType;
import lombok.Data;

@Data
public class AddRouterRequest {
    private Long routerId;
    private Long storeId;
    private Integer circuitNumber;
    private String circuitName;
    private String circuitType;
    private Integer modbusAddress;
    private Integer slaveId;
    private Boolean isControllable;
    private String routerIP;
    private String routerPort;
}
