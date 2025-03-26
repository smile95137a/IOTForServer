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
    private Long routerNumber;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime updateTime;
}
