package com.frontend.config.convert;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.MapperConfig;
import org.mapstruct.ReportingPolicy;

@MapperConfig(
    componentModel = "spring",                  // Spring 自動註冊 Bean
    unmappedTargetPolicy = ReportingPolicy.IGNORE, // 忽略沒對應到的欄位，不要報錯
    injectionStrategy = InjectionStrategy.CONSTRUCTOR // 建構子注入
)
public interface GlobalMapperConfig {
}
