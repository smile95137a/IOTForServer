package com.frontend.req.banner;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BannerReq {

    @NotNull
    private String status; // 讓前端傳入字串，後端轉換 Enum
    private Long newsId;
    private String bannerUid;
}
