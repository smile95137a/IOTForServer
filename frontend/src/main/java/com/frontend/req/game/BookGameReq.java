package com.frontend.req.game;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.frontend.utils.LocalDateTimeDeserializer;
import com.frontend.utils.ZonedDateTimeDeserializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookGameReq {
    private String poolTableUId; // 桌球桌的 ID
    private LocalDate bookDate; // 預約日期
    // 使用 ZonedDateTime 並指定日期格式，包括時區偏移
    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy/MM/dd HH:mm")
    private LocalDateTime endTime;
}
