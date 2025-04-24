package com.frontend.service;

import com.frontend.entity.banner.Banner;
import com.frontend.entity.news.News;
import com.frontend.entity.user.User;
import com.frontend.enums.NewsStatus;
import com.frontend.repo.BannerRepository;
import com.frontend.repo.NewsRepository;
import com.frontend.repo.UserRepository;
import com.frontend.req.NewsReq;
import com.frontend.utils.RandomUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BannerRepository bannerRepository;

    // Create or Update News
    public News saveNews(NewsReq newsReq, Long userId) {
        News news = convertToNews(newsReq, userId);
        return newsRepository.save(news);
    }

    public News convertToNews(NewsReq newsReq , Long userId) {
        Optional<User> byId = userRepository.findById(userId);
        return News.builder()
                .newsUid(RandomUtils.genRandom(24))  // 生成随机 UID
                .title(newsReq.getTitle())
                .content(newsReq.getContent())
                .status(newsReq.getStatus())
                .createdDate(LocalDateTime.now())  // 设置创建时间
                .imageUrl("")  // 默认图片列表为空（可以根据需求修改）
                .author(byId.get().getName())  // 需要额外参数传入
                .isRead(false)  // 默认为未读
                .build();
    }


    // Retrieve All News
    public List<News> getAllNews() {
        return newsRepository.findAll();
    }

    // Retrieve News by ID
    public Optional<News> getNewsById(String uid) {
        return newsRepository.findByNewsUid(uid);
    }

    // Delete News by ID
    @Transactional
    public void deleteNewsById(String uid) throws Exception {
        News news = newsRepository.findByNewsUid(uid).get();
        List<Banner> banner = bannerRepository.findAllByNewsId(news.getId());
        if(!banner.isEmpty()){
            throw new Exception("目前有綁定banner不能刪除");
        }
        bannerRepository.deleteByNewsId(news.getId());
        newsRepository.deleteByNewsUid(uid);
    }

    // Retrieve News by Status
    public List<News> getNewsByStatus(NewsStatus status) {
        return newsRepository.findByStatus(status);
    }

    public News updateNews(String newsId, NewsReq newsReq , Long userId) {
        return newsRepository.findByNewsUid(newsId).map(existingNews -> {
            existingNews.setTitle(newsReq.getTitle());
            existingNews.setContent(newsReq.getContent());
            existingNews.setStatus(newsReq.getStatus());
            existingNews.setUpdatedDate(LocalDateTime.now());
            existingNews.setAuthor(userRepository.findById(userId).get().getName());
            return newsRepository.save(existingNews);
        }).orElseThrow(() -> new RuntimeException("News not found with id: " + newsId));
    }

    public void uploadProductImg(Long id, String uploadedFilePath) {
        News news = newsRepository.findById(id).orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        if(news != null){
            news.setImageUrl(uploadedFilePath);
            newsRepository.save(news);
        }
    }
}
