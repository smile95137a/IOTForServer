package com.frontend.res.router;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RouterResponse {
    private Long id;
    private String equipmentName;
    private Boolean status;
    private String uid;
    private Long routerNumber;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
