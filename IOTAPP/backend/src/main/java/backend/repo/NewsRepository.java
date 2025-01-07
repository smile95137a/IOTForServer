package backend.repo;

import backend.entity.news.News;
import backend.enums.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByStatus(NewsStatus status);
}
