package com.frontend.res.router;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RouterResponse {
    private Long id;
    private String equipmentName;
    private Boolean status;
    private String uid;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;
    private Integer circuitNumber;
    private String circuitName;
    private String circuitType;
    private Integer modbusAddress;
    private Integer slaveId;
    private Boolean isControllable;
    private Boolean currentCircuitStatus;  // 目前迴路開關狀態
}
