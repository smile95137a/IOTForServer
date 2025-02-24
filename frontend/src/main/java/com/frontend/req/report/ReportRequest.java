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
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long storeId;  // 可以為 null
    private Long vendorId; // 可以為 null
    private String periodType;
}
