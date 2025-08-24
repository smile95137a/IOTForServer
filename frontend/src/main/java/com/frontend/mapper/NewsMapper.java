package com.frontend.mapper;

import com.frontend.config.convert.GlobalMapperConfig;
import com.frontend.entity.news.News;
import com.frontend.res.news.NewsRes;
import org.mapstruct.Mapper;

@Mapper(config = GlobalMapperConfig.class)
public interface NewsMapper {
    NewsRes toRes(News news);
}
