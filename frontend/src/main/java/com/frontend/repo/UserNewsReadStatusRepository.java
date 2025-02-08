package com.frontend.repo;

import com.frontend.entity.news.UserNewsReadStatus;
import com.frontend.entity.user.User;
import com.frontend.entity.news.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserNewsReadStatusRepository extends JpaRepository<UserNewsReadStatus, Long> {

    // 根據 user 和 news 查找閱讀狀態
    UserNewsReadStatus findByUserAndNews(User user, News news);
}
