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

        // 先取今天的日期
        Object reportData = reportService.getReportData(
                request.getReportType(),
                isValidDateString(String.valueOf(request.getStartDate())) ? convertToStartOfDay(parseToLocalDate(String.valueOf(request.getStartDate()))) : convertToStartOfDay(null),
                isValidDateString(String.valueOf(request.getEndDate())) ? convertToEndOfDay(parseToLocalDate(String.valueOf(request.getEndDate()))) : convertToEndOfDay(null),
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
        return dateStr != null && !dateStr.isEmpty() && !dateStr.isBlank();
    }

    /**
     * 将字符串解析为LocalDate对象
     */
    private LocalDate parseToLocalDate(String dateStr) {
        try {
            // 根据您的日期格式进行调整，以下假设格式为yyyy-MM-dd
            return LocalDate.parse(dateStr);
        } catch (Exception e) {
            // 解析失败时返回当天日期
            return LocalDate.now();
        }
    }
}
