package com.frontend.service;

import com.frontend.entity.banner.Banner;
import com.frontend.entity.news.News;
import com.frontend.entity.user.User;
import com.frontend.enums.BannerStatus;
import com.frontend.repo.BannerRepository;
import com.frontend.repo.NewsRepository;
import com.frontend.req.banner.BannerReq;
import com.frontend.res.banner.BannerRes;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BannerService {

    private final BannerRepository bannerRepository;
    private final NewsRepository newsRepository;

    public BannerService(BannerRepository bannerRepository, NewsRepository newsRepository) {
        this.bannerRepository = bannerRepository;
        this.newsRepository = newsRepository;
    }

    // 取得所有 Banner（包含關聯的 News）
    public List<BannerRes> getAllBanners() {
        // 获取所有 Banner 实体
        List<Banner> banners = bannerRepository.findAllWithNews();

        // 转换成 BannerRes 列表
        return banners.stream()
                .map(banner -> new BannerRes(
                        banner.getBannerId(),
                        banner.getBannerUid(),
                        banner.getImageUrl(),
                        banner.getStatus(),
                        banner.getNews() // 如果需要，可以深度复制 news 对象
                ))
                .collect(Collectors.toList());
    }

    // 透過 ID 取得 Banner（包含關聯的 News）
    public Optional<BannerRes> getBannerById(Long id) {
        Optional<Banner> banner = bannerRepository.findByIdWithNews(id);

        // 如果 Banner 存在，则转换为 BannerRes
        return banner.map(b -> {
            // 进行手动映射
            BannerRes bannerRes = new BannerRes(
                    b.getBannerId(),
                    b.getBannerUid(),
                    b.getImageUrl(),
                    b.getStatus(),
                    b.getNews()  // 这里直接返回 News 对象，或者根据需要选择字段
            );
            return bannerRes;
        });
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
        banner.setBannerUid(UUID.randomUUID().toString());
        banner.setStatus(status);
        banner.setNews(news);
        banner.setCreatedAt(java.time.LocalDateTime.now());
        banner.setUpdatedAt(java.time.LocalDateTime.now());

        return bannerRepository.save(banner);
    }

    public Banner updateBanner(Long id, String bannerUid, BannerStatus status, Long newsId) {
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));



        // 確保新聞存在
        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));
// 更新 Banner 的欄位
        banner.setBannerUid(bannerUid);
        banner.setStatus(status);
        banner.setUpdatedAt(java.time.LocalDateTime.now());
        banner.setNews(news);
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
