package com.frontend.controller;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.req.report.ReportRequest;
import com.frontend.service.ReportService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/b/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getReportData(@RequestBody ReportRequest request) {
        // 从 request 中提取报告类型、时间范围、storeId、vendorId 和 periodType（如果有）
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        Map<String, Object> reportData = reportService.getReportData(
                request.getReportType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStoreId(),
                request.getVendorId(),
                request.getPeriodType() // 新增的字段用于选择时间分组类型
                ,id
        );

        // 返回响应
        ApiResponse<Map<String, Object>> success = ResponseUtils.success(reportData);
        return ResponseEntity.ok(success);
    }

}
