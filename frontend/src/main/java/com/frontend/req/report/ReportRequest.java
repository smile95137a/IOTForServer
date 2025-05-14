package com.frontend.req.report;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ReportRequest {
    private String reportType;
    private String startDate;  // 確保這是字符串格式的日期，如 "2025-05-01"
    private String endDate;    // 確保這是字符串格式的日期，如 "2025-05-14"
    private Long storeId;  // 可以為 null
    private Long vendorId; // 可以為 null
    private String periodType;
}
