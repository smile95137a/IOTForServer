package com.frontend.service;

import com.frontend.entity.role.Role;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.res.report.TransactionSummary;
import com.frontend.res.user.UserRemainingBalanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
public class ReportService {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;

    @Autowired
    private GameTransactionRecordRepository gameTransactionRecordRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private VendorRepository vendorRepository;

    @Autowired
    private UserRepository userRepository;

    public Object getReportData(String reportType, LocalDateTime startDate, LocalDateTime endDate, Long storeId, Long vendorId, String periodType, Long userId) {
        Store store = (storeId != null) ? storeRepository.findById(storeId).orElse(null) : null;
        Vendor vendor = (vendorId != null) ? vendorRepository.findById(vendorId).orElse(null) : null;
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return Collections.singletonMap("error", "User not found");
        }

        Set<Role> roles = user.getRoles();
        int userRole = roles.stream().anyMatch(role -> role.getId() == 1) ? 1 : 2; // 1=Admin, 2=Vendor

        switch (reportType) {
            case "DepositAmount":
                return (periodType != null && !periodType.isEmpty())
                        ? convertToPeriodData2(
                        transactionRecordRepository.getTotalDepositAmountByPeriod(periodType, startDate, endDate),
                        periodType,
                        startDate,
                        endDate)
                        : new ArrayList<>();

            case "ConsumptionAmount":
                return (periodType != null && !periodType.isEmpty())
                        ? convertToPeriodData2(
                        gameTransactionRecordRepository.getTotalConsumptionByPeriod(periodType, startDate, endDate),
                        periodType,
                        startDate,
                        endDate)
                        : new ArrayList<>();

            case "StoreRevenue":
                if (periodType != null && !periodType.isEmpty()) {
                    List<Object[]> storeRevenueData;
                    if (userRole == 1) { // Admin
                        storeRevenueData = (store != null && store.getName() != null)
                                ? gameTransactionRecordRepository.getStoreRevenueByPeriod(periodType, store.getName(), startDate, endDate)
                                : gameTransactionRecordRepository.getStoreRevenueByPeriodForAdmin(periodType, startDate, endDate);
                    } else { // Vendor
                        storeRevenueData = (store != null)
                                ? gameTransactionRecordRepository.getStoreRevenueByPeriod(periodType, store.getName(), startDate, endDate)
                                : new ArrayList<>();
                    }
                    return convertToPeriodData2(storeRevenueData, periodType, startDate, endDate);
                }
                return new ArrayList<>();

            case "VendorRevenue":
                if (periodType != null && !periodType.isEmpty()) {
                    List<Object[]> vendorRevenueData;
                    if (userRole == 1) { // Admin
                        vendorRevenueData = (vendor != null && vendor.getName() != null)
                                ? gameTransactionRecordRepository.getVendorRevenueByPeriod(periodType, vendor.getName(), startDate, endDate)
                                : gameTransactionRecordRepository.getVendorRevenueByPeriodForAdmin(periodType, startDate, endDate);
                    } else { // Vendor
                        vendorRevenueData = (vendor != null)
                                ? gameTransactionRecordRepository.getVendorRevenueByPeriod(periodType, vendor.getName(), startDate, endDate)
                                : new ArrayList<>();
                    }
                    return convertToPeriodData2(vendorRevenueData, periodType, startDate, endDate);
                }
                return new ArrayList<>();

            case "RemainingBalance":
                List<Object[]> userBalances = userRepository.getAllUserRemainingBalance();
                List<Map<String, Object>> balanceDetails = new ArrayList<>();

                for (Object[] userBalance : userBalances) {
                    Long userIdBalance = (Long) userBalance[0];
                    String userName = (String) userBalance[1];
                    int remainingBalance = (int) userBalance[2];

                    Map<String, Object> balanceEntry = new HashMap<>();
                    balanceEntry.put("userId", userIdBalance);
                    balanceEntry.put("userName", userName);
                    balanceEntry.put("remainingBalance", remainingBalance);
                    balanceDetails.add(balanceEntry);
                }

                return balanceDetails;

            case "DepositCount":
                return (periodType != null && !periodType.isEmpty())
                        ? convertToPeriodData2(
                        transactionRecordRepository.getDepositCountByPeriod(periodType, startDate, endDate),
                        periodType,
                        startDate,
                        endDate)
                        : new ArrayList<>();

            case "ConsumptionCount":
                return (periodType != null && !periodType.isEmpty())
                        ? convertToPeriodData2(
                        transactionRecordRepository.getConsumptionCountByPeriod(periodType, startDate, endDate),
                        periodType,
                        startDate,
                        endDate)
                        : new ArrayList<>();

            case "UserCount":
                if (userRole == 1) { // Admin 才能看會員數
                    return (periodType != null && !periodType.isEmpty())
                            ? convertToPeriodData2(
                            transactionRecordRepository.getUserCountByPeriod(periodType, startDate, endDate),
                            periodType,
                            startDate,
                            endDate)
                            : new ArrayList<>();
                } else {
                    return Collections.singletonMap("error", "權限不足~");
                }
            default:
                return Collections.singletonMap("error", "Invalid report type");
        }
    }


    /**
     * 转换数据库查询结果为标准 JSON 格式
     */
    /**
     * 將資料庫查詢結果轉換為標準JSON格式，並填充缺失的日期
     * @param data 資料庫查詢結果
     * @param periodType 報表類型（DAY, WEEK, MONTH, YEAR）
     * @param startDate 開始日期
     * @param endDate 結束日期
     * @return 完整的報表數據
     */
    private List<Map<String, Object>> convertToPeriodData2(
            List<Object[]> data,
            String periodType,
            LocalDateTime startDate,
            LocalDateTime endDate) {

        // 首先將查詢結果轉換為Map，以便快速查找
        Map<String, Object> dataMap = new HashMap<>();
        for (Object[] record : data) {
            if (record[0] != null) {
                dataMap.put(record[0].toString(), record[1]);
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();

        // 根據不同的報表類型處理日期
        switch (periodType) {
            case "DAY":
                // 日報表：生成從startDate到endDate的每一天
                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                LocalDate currentDate = startDate.toLocalDate();
                LocalDate lastDate = endDate.toLocalDate();

                while (!currentDate.isAfter(lastDate)) {
                    String dateKey = currentDate.format(dayFormatter);
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dateTime", dateKey);
                    // 如果這一天有數據就使用實際數據，否則用0
                    entry.put("amount", dataMap.getOrDefault(dateKey, 0));
                    result.add(entry);

                    currentDate = currentDate.plusDays(1);
                }
                break;

            case "WEEK":
                // 週報表：生成從startDate到endDate的每一週
                LocalDate weekStart = startDate.toLocalDate();
                // 調整到週一開始
                weekStart = weekStart.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate weekEnd = endDate.toLocalDate();

                while (!weekStart.isAfter(weekEnd)) {
                    int year = weekStart.getYear();
                    int week = weekStart.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                    String weekKey = year + "-" + String.format("%02d", week);

                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dateTime", weekKey);
                    entry.put("amount", dataMap.getOrDefault(weekKey, 0));
                    result.add(entry);

                    weekStart = weekStart.plusWeeks(1);
                }
                break;

            case "MONTH":
                // 月報表：生成從startDate到endDate的每一個月
                DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
                LocalDate monthStart = startDate.toLocalDate().withDayOfMonth(1);
                LocalDate monthEnd = endDate.toLocalDate();

                while (!monthStart.isAfter(monthEnd)) {
                    String monthKey = monthStart.format(monthFormatter);
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dateTime", monthKey);
                    entry.put("amount", dataMap.getOrDefault(monthKey, 0));
                    result.add(entry);

                    monthStart = monthStart.plusMonths(1);
                }
                break;

            case "YEAR":
                // 年報表：生成從startDate到endDate的每一年
                int startYear = startDate.getYear();
                int endYear = endDate.getYear();

                for (int year = startYear; year <= endYear; year++) {
                    String yearKey = String.valueOf(year);
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("dateTime", yearKey);
                    entry.put("amount", dataMap.getOrDefault(yearKey, 0));
                    result.add(entry);
                }
                break;
        }

        return result;
    }

}
