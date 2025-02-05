package backend.init;

import backend.entity.news.News;
import backend.enums.NewsStatus;
import backend.repo.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Order(3)
public class NewsInit implements CommandLineRunner {

    @Autowired
    private NewsRepository newsRepository;

    @Override
    public void run(String... args) throws Exception {
        // 检查是否已经有新闻数据，避免重复初始化
        if (newsRepository.count() == 0) {
            // 创建一些新闻数据
            createNews("New Year Celebration", "Join us for the biggest New Year party!",
                    Arrays.asList("image1.jpg", "image2.jpg"), NewsStatus.AVAILABLE);
            createNews("Product Launch", "We're launching a new product this month!",
                    Arrays.asList("launch1.jpg", "launch2.jpg"), NewsStatus.UNAVAILABLE);
            createNews("Holiday Sale", "Don't miss out on our holiday sale!",
                    Arrays.asList("sale1.jpg", "sale2.jpg", "sale3.jpg"), NewsStatus.AVAILABLE);
        }
    }

    private void createNews(String title, String content, List<String> imageUrls, NewsStatus status) {
        News news = News.builder()
                .newsUid(UUID.randomUUID().toString())  // 使用 UUID 生成唯一新闻 UID
                .title(title)
                .content(content)
                .imageUrls(imageUrls)
                .status(status)
                .createdDate(LocalDateTime.now())
                .updatedDate(LocalDateTime.now())
                .author("Admin")
                .build();

        newsRepository.save(news);
    }
}
