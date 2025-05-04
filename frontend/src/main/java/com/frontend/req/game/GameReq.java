package com.frontend.req.game;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GameReq {
    private String gameId;

    private String poolTableUId;

    private Long poolTableId;

    private Boolean confirm;  // ✅ 新增此欄位，用來告訴後端是否為確認開台
}
