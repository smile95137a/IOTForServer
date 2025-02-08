package com.frontend.entity.news;

import com.frontend.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_news_read_status")
public class UserNewsReadStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "news_id", nullable = false)
    private News news;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus; // true: 已讀, false: 未讀

    @Column(name = "read_at")
    private LocalDateTime readAt; // 讀取時間
}
