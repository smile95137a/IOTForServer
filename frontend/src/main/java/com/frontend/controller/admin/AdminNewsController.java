package com.frontend.controller.admin;

import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.enums.NewsStatus;
import com.frontend.config.message.ApiResponse;
import com.frontend.entity.news.News;
import com.frontend.req.NewsReq;
import com.frontend.service.NewsService;
import com.frontend.service.UserNewsService;
import com.frontend.utils.ImageUtil;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/b/news")
public class AdminNewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserNewsService userNewsService;  // 注入 UserNewsService

    // 获取所有新闻并检查是否已读
    @GetMapping
    public ResponseEntity<ApiResponse<List<News>>> getAllNews() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        List<News> newsList = newsService.getAllNews();

        // 如果是已登錄使用者，檢查每篇新聞是否已讀
        if (userId != null) {
            for (News news : newsList) {
                boolean isRead = userNewsService.isNewsReadByUser(userId, news.getId());
                news.setIsRead(isRead);  // 设置已读状态
            }
        } else {
            // 对于未登录用户，只显示公告，但不检查已读状态
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
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = null;

        // 检查是否已登录，如果已登录，获取 userId
        if (securityUser != null) {
            userId = securityUser.getId();
        }

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
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = null;

        // 检查是否已登录，如果已登录，获取 userId
        if (securityUser != null) {
            userId = securityUser.getId();
        }

        List<News> newsList = newsService.getNewsByStatus(status);

        // 如果是已登录用户，检查每篇新闻是否已读
        if (userId != null) {
            for (News news : newsList) {
                boolean isRead = userNewsService.isNewsReadByUser(userId, news.getId());
                news.setIsRead(isRead);  // 设置已读状态
            }
        } else {
            // 对于未登录用户，只显示公告，但不检查已读状态
            for (News news : newsList) {
                news.setIsRead(false);  // 未登录用户默认为未读
            }
        }

        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }


    // 创建或更新新闻
    @PostMapping
    public ResponseEntity<ApiResponse<News>> saveNews(@RequestBody NewsReq news) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        News savedNews = newsService.saveNews(news , userId);
        ApiResponse<News> success = ResponseUtils.success(savedNews);
        return ResponseEntity.ok(success);
    }

    @PutMapping("/{uid}")
    public ResponseEntity<ApiResponse<News>> updateNews(
            @PathVariable String uid,
            @RequestBody NewsReq newsReq) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long userId = securityUser.getId();
        News updatedNews = newsService.updateNews(uid, newsReq , userId);
        return ResponseEntity.ok(ResponseUtils.success(updatedNews));
    }

    // 根据 ID 删除新闻
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deleteNewsById(@PathVariable String uid) {
        newsService.deleteNewsById(uid);
        ApiResponse<Void> success = ResponseUtils.success(null);
        return ResponseEntity.ok(success);
    }

    @PostMapping("/{id}/upload-profile-image")
    public ResponseEntity<?> uploadProfileImages(@PathVariable Long id, @RequestParam("file") List<MultipartFile> files) {
        try {
            if (files == null || files.isEmpty()) {
                return ResponseEntity.badRequest().body(ResponseUtils.error(400, "文件不能為空", null));
            }

            // 上傳所有文件，並獲取文件路徑列表
            List<String> uploadedFilePaths = new ArrayList<>();
            for (MultipartFile file : files) {
                String uploadedFilePath = ImageUtil.upload(file);
                uploadedFilePaths.add(uploadedFilePath);
            }

            // 存儲到數據庫
            newsService.uploadProductImg(id, uploadedFilePaths);

            ApiResponse<List<String>> response = ResponseUtils.success(200, "文件上傳成功", uploadedFilePaths);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<String> response = ResponseUtils.error(500, "文件上傳失敗", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
