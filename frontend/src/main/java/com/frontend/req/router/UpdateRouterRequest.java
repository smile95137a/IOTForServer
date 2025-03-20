package com.frontend.req.router;

import lombok.Data;

@Data
public class UpdateRouterRequest {
    private String routerType;
    private Long number;
    private Boolean status;
}
