package com.frontend.service;

import com.frontend.entity.role.Role;
import com.frontend.entity.store.Store;
import com.frontend.entity.user.User;
import com.frontend.entity.vendor.Vendor;
import com.frontend.repo.*;
import com.frontend.res.user.UserRemainingBalanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
                        ? convertToPeriodData(transactionRecordRepository.getTotalDepositAmountByPeriod(periodType, startDate, endDate))
                        : new ArrayList<>();

            case "ConsumptionAmount":
                return (periodType != null && !periodType.isEmpty())
                        ? convertToPeriodData(gameTransactionRecordRepository.getTotalConsumptionByPeriod(periodType, startDate, endDate))
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
                    return convertToPeriodData(storeRevenueData);
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
                    return convertToPeriodData(vendorRevenueData);
                }
                return new ArrayList<>();

            case "RemainingBalance":
                List<Object[]> userBalances = userRepository.getAllUserRemainingBalance();
                int totalRemainingBalance = 0;
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

                    totalRemainingBalance = remainingBalance;
                }

                return balanceDetails;

            default:
                return Collections.singletonMap("error", "Invalid report type");
        }
    }


    /**
     * 转换数据库查询结果为标准 JSON 格式
     */
    private List<Map<String, Object>> convertToPeriodData(List<Object[]> data) {
        List<Map<String, Object>> formattedList = new ArrayList<>();
        for (Object[] record : data) {
            Map<String, Object> entry = new HashMap<>();
            entry.put("dateTime", record[0]);  // 日期
            entry.put("amount", record[1]);    // 金额
            formattedList.add(entry);
        }
        return formattedList;
    }

}
