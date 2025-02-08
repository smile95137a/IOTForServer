package com.frontend.req.banner;

import lombok.Data;

@Data
public class BannerReq {
    private String bannerUid;
    private String status; // 讓前端傳入字串，後端轉換 Enum
    private Long newsId;
}
