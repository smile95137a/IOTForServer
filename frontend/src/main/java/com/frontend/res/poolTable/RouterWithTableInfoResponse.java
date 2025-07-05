package com.frontend.res.poolTable;

import lombok.Data;

@Data
public class RouterWithTableInfoResponse {
    private Long id;
    private String name;
    private boolean isAssociated;  // 只要知道是否已關聯桌台即可
}