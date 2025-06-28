package com.frontend.req.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouterCircuitRequest {
    private Integer circuitNumber;
    private String circuitName;
    private String circuitType;
    private Integer modbusAddress;
    private Integer slaveId;
    private Boolean isControllable;
    private String routerIP;
    private String routerPort;
    
}