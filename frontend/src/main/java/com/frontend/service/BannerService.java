package com.frontend.service;

import com.frontend.entity.banner.Banner;
import com.frontend.entity.news.News;
import com.frontend.entity.user.User;
import com.frontend.enums.BannerStatus;
import com.frontend.mapper.BannerMapper;
import com.frontend.repo.BannerRepository;
import com.frontend.repo.NewsRepository;
import com.frontend.req.banner.BannerReq;
import com.frontend.res.banner.BannerRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BannerService {

    @Autowired
    private BannerRepository bannerRepository;

    @Autowired
    private NewsRepository newsRepository;

    @Autowired
    private BannerMapper bannerMapper;

    // 取得所有 Banner（包含關聯的 News）
    public List<BannerRes> getAllBanners() {
        List<Banner> banners = bannerRepository.findAllWithNews();

        return bannerMapper.toResList(
                banners.stream()
                        .filter(banner -> banner.getStatus() == BannerStatus.AVAILABLE)
                        .toList()
        );
    }

    // 透過 ID 取得 Banner（包含關聯的 News）
    public BannerRes getBannerById(Long id) {
        Banner banner = bannerRepository.findByIdWithNews(id)
                .orElseThrow(() -> new RuntimeException("Banner not found")); // ✅ 避免 NullPointer / get()

        return bannerMapper.toRes(banner); // ✅ 交給 MapStruct 轉換
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
        banner.setImageUrl("");
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
        banner.setBannerUid(banner.getBannerUid());
        banner.setStatus(status);
        banner.setNews(banner.getNews());
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
            banner.setImageUrl(uploadedFilePath);
            bannerRepository.save(banner);
    }

    public List<BannerRes> getAllBannersByB() {
        // 获取所有 Banner 实体
        List<Banner> banners = bannerRepository.findAllWithNews();

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
}
