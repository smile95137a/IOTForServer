package backend.service;

import backend.entity.news.News;
import backend.enums.NewsStatus;
import backend.repo.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    // Create or Update News
    public News saveNews(News news) {
        return newsRepository.save(news);
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
    public void deleteNewsById(String uid) {
        newsRepository.deleteByNewsUid(uid);
    }

    // Retrieve News by Status
    public List<News> getNewsByStatus(NewsStatus status) {
        return newsRepository.findByStatus(status);
    }
}
