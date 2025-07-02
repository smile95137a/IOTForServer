package com.frontend.req.router;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CircuitControlRequest {
    private Long routerId;
    private boolean targetStatus;  // true=開, false=關
    private Long storeId;
}