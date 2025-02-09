package com.frontend.req.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class CheckoutReq {

    private String payType;

    private String gameId;
}
