package com.frontend.res.poolTable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoolTableRes {

    private Integer deposit;

    private String gameId;

    private Long poolTableId;

}
