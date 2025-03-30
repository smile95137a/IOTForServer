package com.frontend.repo;

import com.frontend.entity.game.BookGame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookGameRepository extends JpaRepository<BookGame, Long> {
    BookGame findByGameId(String gameId);

    List<BookGame> findByUserUId(String uid);
}
