package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.news.News;
import backend.enums.NewsStatus;
import backend.service.NewsService;
import backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    // 创建或更新新闻
    @PostMapping
    public ResponseEntity<ApiResponse<News>> saveNews(@RequestBody News news) {
        News savedNews = newsService.saveNews(news);
        ApiResponse<News> success = ResponseUtils.success(savedNews);
        return ResponseEntity.ok(success);
    }

    // 获取所有新闻
    @GetMapping
    public ResponseEntity<ApiResponse<List<News>>> getAllNews() {
        List<News> newsList = newsService.getAllNews();
        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }

    // 根据 ID 获取新闻
    @GetMapping("/{uid}")
    public ResponseEntity<ApiResponse<News>> getNewsById(@PathVariable String uid) {
        Optional<News> news = newsService.getNewsById(uid);
        if (news.isEmpty()) {
            ApiResponse<News> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<News> success = ResponseUtils.success(news.get());
        return ResponseEntity.ok(success);
    }

    // 根据 ID 删除新闻
    @DeleteMapping("/{uid}")
    public ResponseEntity<ApiResponse<Void>> deleteNewsById(@PathVariable String uid) {
        newsService.deleteNewsById(uid);
        ApiResponse<Void> success = ResponseUtils.success(null);
        return ResponseEntity.ok(success);
    }

    // 根据状态获取新闻
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<News>>> getNewsByStatus(@PathVariable NewsStatus status) {
        List<News> newsList = newsService.getNewsByStatus(status);
        ApiResponse<List<News>> success = ResponseUtils.success(newsList);
        return ResponseEntity.ok(success);
    }
}
