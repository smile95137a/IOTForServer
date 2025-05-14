package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.req.report.ReportRequest;
import com.frontend.service.ReportService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

        // 調試日期參數
        System.out.println("Raw startDate: " + request.getStartDate());
        System.out.println("Raw endDate: " + request.getEndDate());

        // 處理開始日期
        LocalDateTime startDateTime;
        if (isValidDateString(request.getStartDate())) {
            LocalDate parsedStartDate = parseToLocalDate(request.getStartDate());
            System.out.println("Parsed startDate: " + parsedStartDate);
            startDateTime = convertToStartOfDay(parsedStartDate);
        } else {
            startDateTime = convertToStartOfDay(null);  // 使用當前日期
        }

        // 處理結束日期
        LocalDateTime endDateTime;
        if (isValidDateString(request.getEndDate())) {
            LocalDate parsedEndDate = parseToLocalDate(request.getEndDate());
            System.out.println("Parsed endDate: " + parsedEndDate);
            endDateTime = convertToEndOfDay(parsedEndDate);
        } else {
            endDateTime = convertToEndOfDay(null);  // 使用當前日期
        }

        System.out.println("Final startDateTime: " + startDateTime);
        System.out.println("Final endDateTime: " + endDateTime);

        // 呼叫服務
        Object reportData = reportService.getReportData(
                request.getReportType(),
                startDateTime,
                endDateTime,
                request.getStoreId(),
                request.getVendorId(),
                request.getPeriodType(),
                id
        );

        ApiResponse<Object> success = ResponseUtils.success(reportData);
        return ResponseEntity.ok(success);
    }

    /**
     * 将日期转换为当天的开始时间 (00:00:00)
     * 如果日期为空，则返回当天的开始时间
     */
    private LocalDateTime convertToStartOfDay(LocalDate date) {
        if (date == null) {
            // 如果没有提供日期，使用当天日期
            return LocalDate.now().atStartOfDay();
        }
        // 返回指定日期的开始时间 (00:00:00)
        return date.atStartOfDay();
    }

    /**
     * 将日期转换为当天的结束时间 (23:59:59.999999999)
     * 如果日期为空，则返回当天的结束时间
     */
    private LocalDateTime convertToEndOfDay(LocalDate date) {
        if (date == null) {
            // 如果没有提供日期，使用当天日期
            return LocalDate.now().atTime(23, 59, 59, 999999999);
        }
        // 返回指定日期的结束时间 (23:59:59.999999999)
        return date.atTime(23, 59, 59, 999999999);
    }

    /**
     * 检查字符串是否为有效的日期字符串
     */
    private boolean isValidDateString(String dateStr) {
        if (dateStr == null || dateStr.isEmpty() || dateStr.isBlank()) {
            return false;
        }

        try {
            // 嘗試解析日期
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            // 如果解析失敗，記錄錯誤並返回 false
            System.err.println("Invalid date string: " + dateStr + ". Error: " + e.getMessage());
            return false;
        }
    }

    /**
     * 将字符串解析为LocalDate对象
     */
    private LocalDate parseToLocalDate(String dateStr) {
        try {
            // 嘗試解析日期
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            // 記錄錯誤並提供更詳細的信息
            System.err.println("Failed to parse date string: " + dateStr + ". Error: " + e.getMessage());
            System.err.println("Using current date as fallback.");
            return LocalDate.now();
        }
    }
}
