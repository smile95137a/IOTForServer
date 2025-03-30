package com.frontend.req.router;

import com.frontend.enums.RouterType;
import lombok.Data;

@Data
public class AddRouterRequest {
    private Long storeId;
    private RouterType routerType;
    private Long number;
}
