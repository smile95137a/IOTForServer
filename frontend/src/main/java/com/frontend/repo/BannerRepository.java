package com.frontend.repo;

import com.frontend.entity.banner.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BannerRepository extends JpaRepository<Banner, Long> {

    // 查詢所有 Banner，並關聯對應的 News
    @Query("SELECT b FROM Banner b LEFT JOIN FETCH b.news")
    List<Banner> findAllWithNews();

    // 透過 ID 查詢 Banner，並關聯對應的 News
    @Query("SELECT b FROM Banner b LEFT JOIN FETCH b.news WHERE b.bannerId = :bannerId")
    Optional<Banner> findByIdWithNews(@Param("bannerId") Long bannerId);

    void deleteByNewsId(Long id);
}
