package com.frontend.repo;

import com.frontend.entity.game.GameOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameOrderRepository extends JpaRepository<GameOrder, Integer> {
}
