package com.frontend.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.OffsetDateTime;

public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String date = p.getText().trim();
        // 如果包含時區，將其轉換成 LocalDateTime
        if (date.contains("+") || date.contains("Z")) {
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(date);
            return offsetDateTime.toLocalDateTime();
        } else {
            // 直接解析無時區的 LocalDateTime
            return LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }
}
