package com.frontend.req.topOp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopOpReq {

    private Integer price;

    private String payType;
}
