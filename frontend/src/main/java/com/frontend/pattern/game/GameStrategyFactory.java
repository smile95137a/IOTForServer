package com.frontend.pattern.game;

import com.frontend.req.game.GameReq;
import org.springframework.stereotype.Service;

@Service
public class GameStrategyFactory {

    private final ImmediateStartStrategy immediateStartStrategy;
    private final BookingStartStrategy bookingStartStrategy; // 新增

    public GameStrategyFactory(ImmediateStartStrategy immediateStartStrategy,
                               BookingStartStrategy bookingStartStrategy) {
        this.immediateStartStrategy = immediateStartStrategy;
        this.bookingStartStrategy = bookingStartStrategy;
    }

    public GameStartStrategy getStartStrategy(GameReq req) {
        // 根據 req 的 type 決定策略
        if ("BOOKING".equalsIgnoreCase(req.getType())) {
            return bookingStartStrategy;
        }
        return immediateStartStrategy; // 預設使用立即開台
    }
}
