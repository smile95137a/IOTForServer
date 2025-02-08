package com.frontend.service;

import com.frontend.entity.news.UserNewsReadStatus;
import com.frontend.entity.news.News;
import com.frontend.entity.user.User;
import com.frontend.repo.NewsRepository;
import com.frontend.repo.UserNewsReadStatusRepository;
import com.frontend.repo.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserNewsService {

    private final UserNewsReadStatusRepository userNewsReadStatusRepository;
    private final NewsRepository newsRepository;
    private final UserRepository userRepository;

    public UserNewsService(UserNewsReadStatusRepository userNewsReadStatusRepository,
                           NewsRepository newsRepository,
                           UserRepository userRepository) {
        this.userNewsReadStatusRepository = userNewsReadStatusRepository;
        this.newsRepository = newsRepository;
        this.userRepository = userRepository;
    }

    // 判斷使用者是否已讀過某篇新聞
    public boolean isNewsReadByUser(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        UserNewsReadStatus userNewsReadStatus = userNewsReadStatusRepository
                .findByUserAndNews(user, news);

        return userNewsReadStatus != null && userNewsReadStatus.getReadStatus();
    }

    // 標記某篇新聞為已讀
    public void markNewsAsRead(Long userId, Long newsId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        News news = newsRepository.findById(newsId)
                .orElseThrow(() -> new RuntimeException("News not found"));

        // 檢查是否已存在紀錄
        UserNewsReadStatus userNewsReadStatus = userNewsReadStatusRepository
                .findByUserAndNews(user, news);

        if (userNewsReadStatus == null) {
            userNewsReadStatus = UserNewsReadStatus.builder()
                    .user(user)
                    .news(news)
                    .readStatus(true)
                    .readAt(LocalDateTime.now())
                    .build();
        } else {
            userNewsReadStatus.setReadStatus(true);
            userNewsReadStatus.setReadAt(LocalDateTime.now());
        }

        userNewsReadStatusRepository.save(userNewsReadStatus);
    }
}
