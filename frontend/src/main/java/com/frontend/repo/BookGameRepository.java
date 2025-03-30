package com.frontend.repo;

import com.frontend.entity.game.BookGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookGameRepository extends JpaRepository<BookGame, Long> {
    BookGame findByGameId(String gameId);
}
