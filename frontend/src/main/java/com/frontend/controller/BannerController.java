package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.banner.Banner;
import com.frontend.enums.BannerStatus;
import com.frontend.req.banner.BannerReq;
import com.frontend.res.banner.BannerRes;
import com.frontend.res.store.StoreRes;
import com.frontend.service.BannerService;
import com.frontend.utils.ImageUtil;
import com.frontend.utils.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping
public class BannerController {

    private final BannerService bannerService;

    public BannerController(BannerService bannerService) {
        this.bannerService = bannerService;
    }

    // 取得所有 Banner
    @GetMapping("/banner")
    public ResponseEntity<ApiResponse<List<BannerRes>>> getAllBanners() {
        List<BannerRes> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ResponseUtils.success(banners));
    }

    // 透過 ID 取得 Banner
    @GetMapping("/banner/{id}")
    public ResponseEntity<ApiResponse<BannerRes>> getBannerById(@PathVariable Long id) {
        try {
            BannerRes bannerRes = bannerService.getBannerById(id);
            return ResponseEntity.ok(ResponseUtils.success(bannerRes));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResponseUtils.error(9999, "無此banner", null));
        }
    }
}
