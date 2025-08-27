package com.frontend.mapper;

import com.frontend.config.convert.GlobalMapperConfig;
import com.frontend.entity.poolTable.PoolTable;
import com.frontend.entity.router.Router;
import com.frontend.res.poolTable.AdminPoolTableRes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class)
public interface PoolTableMapper {

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "routerId", source = "routers")
    AdminPoolTableRes toRes(PoolTable poolTable);

    List<AdminPoolTableRes> toResList(List<PoolTable> poolTables);

    // 自訂轉換：Router -> Long
    default Long mapRouterToId(Router router) {
        return router != null ? router.getId() : null;
    }
}
