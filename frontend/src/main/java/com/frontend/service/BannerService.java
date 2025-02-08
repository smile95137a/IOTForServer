package com.frontend.service;

import com.frontend.entity.banner.Banner;
import com.frontend.entity.news.News;
import com.frontend.entity.user.User;
import com.frontend.enums.BannerStatus;
import com.frontend.repo.BannerRepository;
import com.frontend.repo.NewsRepository;
import com.frontend.req.banner.BannerReq;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;
    private final NewsRepository newsRepository;

    public BannerService(BannerRepository bannerRepository, NewsRepository newsRepository) {
        this.bannerRepository = bannerRepository;
        this.newsRepository = newsRepository;
    }

    // 取得所有 Banner（包含關聯的 News）
    public List<Banner> getAllBanners() {
        return bannerRepository.findAllWithNews();
    }

    // 透過 ID 取得 Banner（包含關聯的 News）
    public Optional<Banner> getBannerById(Long id) {
        return bannerRepository.findByIdWithNews(id);
    }

    // 新增 Banner
    public Banner createBanner(BannerReq bannerReq) {
        // 轉換 status，默認為 UNAVAILABLE
        BannerStatus status;
        try {
            status = BannerStatus.valueOf(bannerReq.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            status = BannerStatus.UNAVAILABLE;
        }

        // 確保新聞存在
        News news = newsRepository.findById(bannerReq.getNewsId())
                .orElseThrow(() -> new RuntimeException("News not found"));

        // 建立 Banner
        Banner banner = new Banner();
        banner.setBannerUid(bannerReq.getBannerUid());
        banner.setStatus(status);
        banner.setCreatedAt(java.time.LocalDateTime.now());
        banner.setUpdatedAt(java.time.LocalDateTime.now());

        return bannerRepository.save(banner);
    }

    public Banner updateBanner(Long id, String bannerUid, BannerStatus status, Long newsId) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        // 更新 Banner 的欄位
        banner.setBannerUid(bannerUid);
        banner.setStatus(status);
        banner.setUpdatedAt(java.time.LocalDateTime.now());

        // 確保新聞存在
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        return bannerRepository.save(banner);
    }

    // 刪除 Banner
    public void deleteBanner(Long id) {
        if (!bannerRepository.existsById(id)) {
            throw new IllegalArgumentException("Banner 不存在");
        }
        bannerRepository.deleteById(id);
    }

    public void uploadImg(Long bannerId, String uploadedFilePath) {
        Banner banner = bannerRepository.findById(bannerId).get();
        if (banner != null) {
            banner.setImageUrl(uploadedFilePath);
            bannerRepository.save(banner);
            }
    }
}
