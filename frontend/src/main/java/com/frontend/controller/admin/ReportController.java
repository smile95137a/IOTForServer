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
    public ResponseEntity<ApiResponse<Object>> getReportData(@RequestBody ReportRequest request) {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();

        Object reportData = reportService.getReportData(
                request.getReportType(),
                request.getStartDate(),
                request.getEndDate(),
                request.getStoreId(),
                request.getVendorId(),
                request.getPeriodType(),
                id
        );

        ApiResponse<Object> success = ResponseUtils.success(reportData);
        return ResponseEntity.ok(success);
    }

}
