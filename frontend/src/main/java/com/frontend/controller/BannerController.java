package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.entity.banner.Banner;
import com.frontend.enums.BannerStatus;
import com.frontend.req.banner.BannerReq;
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
    public ResponseEntity<List<Banner>> getAllBanners() {
        return ResponseEntity.ok(bannerService.getAllBanners());
    }

    // 透過 ID 取得 Banner
    @GetMapping("/banner/{id}")
    public ResponseEntity<Banner> getBannerById(@PathVariable Long id) {
        Optional<Banner> banner = bannerService.getBannerById(id);
        return banner.map(ResponseEntity::ok)
                     .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 新增 Banner
    @PostMapping("/api/banners")
    public ResponseEntity<Banner> createBanner(
            @RequestBody BannerReq bannerReq) {
        Banner banner = bannerService.createBanner(bannerReq);
        return ResponseEntity.ok(banner);
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
    public ResponseEntity<Void> deleteBanner(@PathVariable Long id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{bannerId}/upload-image")
    public ResponseEntity<?> uploadProfileImage(@PathVariable Long bannerId, @RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseUtils.error(400, "文件不能為空", null));
            }

            String uploadedFilePath = ImageUtil.upload(file);

            bannerService.uploadImg(bannerId, uploadedFilePath);

            ApiResponse<String> response = ResponseUtils.success(200, "文件上傳成功", uploadedFilePath);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = ResponseUtils.error(500, "文件上傳失敗", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
