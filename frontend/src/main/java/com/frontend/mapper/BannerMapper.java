package com.frontend.mapper;

import com.frontend.config.convert.GlobalMapperConfig;
import com.frontend.entity.banner.Banner;
import com.frontend.res.banner.BannerRes;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(config = GlobalMapperConfig.class, uses = { NewsMapper.class })
public interface BannerMapper {
    BannerRes toRes(Banner banner);
    List<BannerRes> toResList(List<Banner> banners);
}
