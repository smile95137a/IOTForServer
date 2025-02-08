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
        Optional<BannerRes> bannerRes = bannerService.getBannerById(id);

        return bannerRes
                .map(b -> ResponseEntity.ok(ResponseUtils.success(b))) // 直接返回已转换的 BannerRes
                .orElseGet(() -> ResponseEntity.ok(ResponseUtils.error(9999, "無此桌台", null)));
    }


    // 新增 Banner
    @PostMapping("/api/banners")
    public ResponseEntity<ApiResponse<Banner>> createBanner(@RequestBody BannerReq bannerReq) {
        Banner banner = bannerService.createBanner(bannerReq);
        return ResponseEntity.ok(ResponseUtils.success(banner));
    }

    // 更新 Banner
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Banner>> updateBanner(
            @PathVariable Long id,
            @RequestBody BannerReq bannerUpdateReq) {

        // 轉換 status，默認為 UNAVAILABLE
        BannerStatus status;
        try {
            status = BannerStatus.valueOf(bannerUpdateReq.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            status = BannerStatus.UNAVAILABLE;
        }

        // 更新 Banner
        Banner banner = bannerService.updateBanner(id, bannerUpdateReq.getBannerUid(), status, bannerUpdateReq.getNewsId());
        return ResponseEntity.ok(ResponseUtils.success(banner));
    }

    // 刪除 Banner
    @DeleteMapping("/api/banners/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.ok(ResponseUtils.success(null));
    }

    // 上傳 Banner 圖片
    @PostMapping("/{bannerId}/upload-image")
    public ResponseEntity<ApiResponse<String>> uploadProfileImage(@PathVariable Long bannerId, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.ok(ResponseUtils.error(400, "文件不能為空", null));
            }

            String uploadedFilePath = ImageUtil.upload(file);
            bannerService.uploadImg(bannerId, uploadedFilePath);

            return ResponseEntity.ok(ResponseUtils.success(200, "文件上傳成功", uploadedFilePath));
        } catch (Exception e) {
            return ResponseEntity.ok(ResponseUtils.error(500, "文件上傳失敗", null));
        }
    }
}
