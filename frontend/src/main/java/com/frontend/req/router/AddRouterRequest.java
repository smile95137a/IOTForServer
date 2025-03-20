package com.frontend.req.router;

import lombok.Data;

@Data
public class AddRouterRequest {
    private Long storeId;
    private String routerType;
    private Long number;
}
