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

    public Map<String, Object> getReportData(String reportType, LocalDateTime startDate, LocalDateTime endDate, Long storeId, Long vendorId , String periodType , Long userId) {
        Map<String, Object> response = new HashMap<>();
        Store store = storeRepository.findById(storeId).get();
        Vendor vendor = vendorRepository.findById(vendorId).get();
        User user = userRepository.findById(userId).get();
        Set<Role> roles = user.getRoles();
        int userRole = roles.stream().anyMatch(role -> role.getId() == 1) ? 1 : 2; // 如果包含角色 1，设置为 Admin (1)，否则设置为厂商 (2)

        switch (reportType) {
            case "Deposit Amount":
                if (periodType != null && !periodType.isEmpty()) {
                    response.put("Deposit Amount by Period", transactionRecordRepository.getTotalDepositAmountByPeriod(periodType, startDate, endDate));
                }
                break;
            case "Consumption Amount":
                if (periodType != null && !periodType.isEmpty()) {
                    response.put("Consumption Amount by Period", gameTransactionRecordRepository.getTotalConsumptionByPeriod(periodType, startDate, endDate));
                }
                break;
            case "Store Revenue":
                // Admin 和厂商都有权限查看，但厂商只能看到自己店铺的数据
                if (periodType != null && !periodType.isEmpty()) {
                    if (userRole == 1) { // Admin 可以看到所有店铺
                        if (store.getName() == null || store.getName().isEmpty()) { // Admin 没有选择店铺
                            response.put("Store Revenue by Period", gameTransactionRecordRepository.getStoreRevenueByPeriodForAdmin(periodType, startDate, endDate));
                        } else { // Admin 选择了店铺
                            response.put("Store Revenue by Period", gameTransactionRecordRepository.getStoreRevenueByPeriod(periodType, store.getName(), startDate, endDate));
                        }
                    } else if (userRole == 2) { // 厂商只能看到自己店铺的数据
                        response.put("Store Revenue by Period", gameTransactionRecordRepository.getStoreRevenueByPeriod(periodType, store.getName(), startDate, endDate));
                    }
                }
                break;
            case "Vendor Revenue":
                if (periodType != null && !periodType.isEmpty()) {
                    // Admin 和厂商都有权限查看，但厂商只能看到自己厂商的数据
                    if (userRole == 1) { // Admin 可以看到所有厂商
                        if (vendor.getName() == null || vendor.getName().isEmpty()) { // Admin 没有选择厂商
                            response.put("Vendor Revenue by Period", gameTransactionRecordRepository.getVendorRevenueByPeriodForAdmin(periodType, startDate, endDate));
                        } else { // Admin 选择了厂商
                            response.put("Vendor Revenue by Period", gameTransactionRecordRepository.getVendorRevenueByPeriod(periodType, vendor.getName(), startDate, endDate));
                        }
                    } else if (userRole == 2) { // 厂商只能看到自己厂商的数据
                        response.put("Vendor Revenue by Period", gameTransactionRecordRepository.getVendorRevenueByPeriod(periodType, vendor.getName(), startDate, endDate));
                    }
                }
                break;
            case "Remaining Balance":
                // 获取所有用户的剩余储值金额
                List<Object[]> userBalances = userRepository.getAllUserRemainingBalance();
                BigDecimal totalRemainingBalance = BigDecimal.ZERO;

                // 把每个用户的余额和总和一起返回
                List<UserRemainingBalanceDTO> balanceDetails = new ArrayList<>();
                for (Object[] userBalance : userBalances) {
                    Long userid = (Long) userBalance[0];
                    String userName = (String) userBalance[1];
                    BigDecimal remainingBalance = (BigDecimal) userBalance[2];
                    balanceDetails.add(new UserRemainingBalanceDTO(userid, userName, remainingBalance));
                    totalRemainingBalance = totalRemainingBalance.add(remainingBalance);  // 计算总余额
                }

                response.put("Remaining Balance Details", balanceDetails);
                response.put("Total Remaining Balance", totalRemainingBalance);  // 返回总余额
                break;
            default:
                response.put("error", "Invalid report type");
                break;
        }

        return response;
    }

}
