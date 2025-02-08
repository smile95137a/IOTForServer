package com.frontend.res.banner;

import com.frontend.entity.news.News;
import com.frontend.enums.BannerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class BannerRes {
    private Long bannerId;

    private String bannerUid;

    private String imageUrl;

    private BannerStatus status;

    private News news;
}
