package com.frontend.repo;

import com.frontend.entity.news.News;
import com.frontend.enums.NewsStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {
    List<News> findByStatus(NewsStatus status);

    Optional<News> findByNewsUid(String newsUid);

    void deleteByNewsUid(String uid);
}
