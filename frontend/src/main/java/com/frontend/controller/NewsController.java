package com.frontend.controller;

import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.enums.NewsStatus;
import com.frontend.config.message.ApiResponse;
import com.frontend.entity.news.News;
import com.frontend.service.NewsService;
import com.frontend.service.UserNewsService;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserNewsService userNewsService;

    // 获取所有新闻并检查是否已读
    @GetMapping
    public ResponseEntity<ApiResponse<List<News>>> getAllNews() {
        // 获取当前登录的用户
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = (securityUser != null) ? securityUser.getId() : null;

        // 获取所有新闻
        List<News> newsList = newsService.getAllNews();

        if (newsList.isEmpty()) {
            // 如果新闻列表为空，打印日志
            System.out.println("No news available.");
        }

        if (userId != null) {
            // 已登录用户，检查每篇新闻是否已读
            for (News news : newsList) {
                boolean isRead = userNewsService.isNewsReadByUser(userId, news.getId());
                news.setIsRead(isRead);  // 设置已读状态
            }
        } else {
            // 未登录用户，默认不显示已读状态
            for (News news : newsList) {
                news.setIsRead(false);  // 未登录用户默认为未读
            }
        }

        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }

    // 根据 ID 获取新闻并标记为已读
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<News>> getNewsById(@PathVariable String uid) {
        // 获取当前登录的用户
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = (securityUser != null) ? securityUser.getId() : null;

        // 获取新闻
        Optional<News> news = newsService.getNewsById(uid);
        if (news.isEmpty()) {
            ApiResponse<News> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }

        News newsItem = news.get();

        // 如果已登录用户，检查该新闻是否已读，如果未读，标记为已读
        if (userId != null) {
            boolean isRead = userNewsService.isNewsReadByUser(userId, newsItem.getId());
            newsItem.setIsRead(isRead);

            if (!isRead) {
                // 将新闻标记为已读
                userNewsService.markNewsAsRead(userId, newsItem.getId());
            }
        } else {
            // 未登录用户则直接设置为未读
            newsItem.setIsRead(false);
        }

        ApiResponse<News> success = ResponseUtils.success(newsItem);
        return ResponseEntity.ok(success);
    }

    // 根据状态获取新闻并检查是否已读
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByStatus(@PathVariable NewsStatus status) {
        // 获取当前登录的用户
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = (securityUser != null) ? securityUser.getId() : null;

        // 获取指定状态的新闻
        List<News> newsList = newsService.getNewsByStatus(status);

        if (newsList.isEmpty()) {
            // 如果新闻列表为空，打印日志
            System.out.println("No news available for status: " + status);
        }

        if (userId != null) {
            // 已登录用户，检查每篇新闻是否已读
            for (News news : newsList) {
                boolean isRead = userNewsService.isNewsReadByUser(userId, news.getId());
                news.setIsRead(isRead);  // 设置已读状态
            }
        } else {
            // 未登录用户，默认不显示已读状态
            for (News news : newsList) {
                news.setIsRead(false);  // 未登录用户默认为未读
            }
        }

        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }

    // 获取所有新闻并检查是否已读
    // 获取所有新闻，不考虑登录状态
    @GetMapping("/query")
    public ResponseEntity<ApiResponse<List<News>>> getAllNewsNoUser() {
        // 获取所有新闻
        List<News> newsList = newsService.getAllNews();

        if (newsList.isEmpty()) {
            // 如果新闻列表为空，打印日志
            System.out.println("No news available.");
        }

        // 未登录用户，默认不显示已读状态
        for (News news : newsList) {
            news.setIsRead(false);  // 未登录用户默认为未读
        }

        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }

    // 根据 ID 获取新闻并标记为已读，不考虑登录状态
    @GetMapping("/query/{uid}")
    public ResponseEntity<ApiResponse<News>> getNewsByIdNoUser(@PathVariable String uid) {
        // 获取新闻
        Optional<News> news = newsService.getNewsById(uid);
        if (news.isEmpty()) {
            ApiResponse<News> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }

        News newsItem = news.get();

        // 未登录用户直接设置为未读
        newsItem.setIsRead(false);

        ApiResponse<News> success = ResponseUtils.success(newsItem);
        return ResponseEntity.ok(success);
    }

    // 根据状态获取新闻并检查是否已读，不考虑登录状态
    @GetMapping("/query/status/{status}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByStatusNoUser(@PathVariable NewsStatus status) {
        // 获取指定状态的新闻
        List<News> newsList = newsService.getNewsByStatus(status);

        if (newsList.isEmpty()) {
            // 如果新闻列表为空，打印日志
            System.out.println("No news available for status: " + status);
        }

        // 未登录用户，默认不显示已读状态
        for (News news : newsList) {
            news.setIsRead(false);  // 未登录用户默认为未读
        }

        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }
}
