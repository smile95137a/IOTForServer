package com.frontend.controller.admin;


import com.frontend.config.message.ApiResponse;
import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.store.Store;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.GameOrderRepository;
import com.frontend.repo.StoreRepository;
import com.frontend.repo.TransactionRecordRepository;
import com.frontend.repo.UserRepository;
import com.frontend.res.transaction.TransactionsRes;
import com.frontend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/b/turnover")
public class turnoverController {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;


    @Autowired
    private GameOrderRepository gameOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StoreRepository storeRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<TransactionsRes>> getTransactionsSummary() {
        // 获取当前登录用户
        UserPrinciple currentUser = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取用户的所有角色ID
        Set<Long> roleIds = getUserRoleIds(currentUser);

        Long vendorId = null;
        List<Long> storeIds = null;
        Long highestRoleId = getHighestPriorityRole(roleIds);

        // 根据角色获取关联ID
        if (roleIds.contains(2L) || highestRoleId == 2L) { // 加盟商（Vendor）
            vendorId = user.getVendor() != null ? user.getVendor().getId() : null;
        } else if (roleIds.contains(5L) || highestRoleId == 5L) { // 店家店长（Store）
            List<Store> userStores = storeRepository.findByUserId(user.getId());
            storeIds = userStores.stream()
                    .map(Store::getId)
                    .collect(Collectors.toList());
        }

        // 获取今天的储值总金额和笔数 (根据角色过滤)
        TransactionsRes todayDeposits = getTodayTotalDeposits(highestRoleId, vendorId, storeIds);

        // 获取今天的消费总金额和笔数 (根据角色过滤)
        TransactionsRes todayConsumption = getTodayTotalConsumption(highestRoleId, vendorId, storeIds);

        // 获取本月的总消费金额和笔数 (根据角色过滤)
        TransactionsRes monthConsumption = getMonthTotalConsumption(highestRoleId, vendorId, storeIds);

        // 獲取儲值總額 (不按店家和廠商過濾)
        TransactionsRes totalDeposits = getTotalDeposits();

        // 獲取消費總額 (按店家和廠商過濾)
        TransactionsRes totalConsumption = getTotalConsumptionFiltered(highestRoleId, vendorId, storeIds);

        // 创建一个汇总对象，适配前端需要的字段名
        TransactionsRes summaryRes = new TransactionsRes();

        // 设置今日充值数据
        summaryRes.setTodayTopupAmount(todayDeposits.getTodayTotalAmount());
        summaryRes.setTodayTopupCount(todayDeposits.getTodayTransactionCount());

        // 设置今日消费数据
        summaryRes.setTodayTotalAmount(todayConsumption.getTodayTotalAmount());
        summaryRes.setTodayTransactionCount(todayConsumption.getTodayTransactionCount());

        // 设置本月消费数据
        summaryRes.setMonthTotalAmount(monthConsumption.getTodayTotalAmount());
        summaryRes.setMonthTransactionCount(monthConsumption.getTodayTransactionCount());

        // 設置儲值總額數據 (不按店家和廠商過濾)
        summaryRes.setTotalDepositAmount(totalDeposits.getTodayTotalAmount());
        summaryRes.setTotalDepositCount(totalDeposits.getTodayTransactionCount());

        // 設置消費總額數據 (按店家和廠商過濾)
        summaryRes.setTotalConsumptionAmount(totalConsumption.getTodayTotalAmount());
        summaryRes.setTotalConsumptionCount(totalConsumption.getTodayTransactionCount());

        // 返回响应
        return ResponseEntity.ok(ResponseUtils.success(200, null, summaryRes));
    }

    // 获取今日储值总金额和笔数 (根据角色过滤)
    private TransactionsRes getTodayTotalDeposits(Long highestRoleId, Long vendorId, List<Long> storeIds) {
        List<TransactionsRes> todayDeposits = transactionRecordRepository.getTodayTotalDeposits(highestRoleId, vendorId, storeIds);
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer transactionCount = 0;

        for (TransactionsRes res : todayDeposits) {
            totalAmount = totalAmount.add(res.getTodayTotalAmount());
            transactionCount += res.getTodayTransactionCount();
        }

        return new TransactionsRes(totalAmount, transactionCount);
    }

    // 获取今日消费总金额和笔数 (根据角色过滤)
    private TransactionsRes getTodayTotalConsumption(Long highestRoleId, Long vendorId, List<Long> storeIds) {
        List<TransactionsRes> todayConsumption = gameOrderRepository.getTodayTotalConsumption(highestRoleId, vendorId, storeIds);
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer transactionCount = 0;

        for (TransactionsRes res : todayConsumption) {
            totalAmount = totalAmount.add(res.getTodayTotalAmount());
            transactionCount += res.getTodayTransactionCount();
        }

        return new TransactionsRes(totalAmount, transactionCount);
    }

    // 获取本月的总消费金额和笔数 (根据角色过滤)
    private TransactionsRes getMonthTotalConsumption(Long highestRoleId, Long vendorId, List<Long> storeIds) {
        List<TransactionsRes> monthConsumption = gameOrderRepository.getMonthTotalConsumption(highestRoleId, vendorId, storeIds);
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer transactionCount = 0;

        for (TransactionsRes res : monthConsumption) {
            totalAmount = totalAmount.add(res.getTodayTotalAmount());
            transactionCount += res.getTodayTransactionCount();
        }

        return new TransactionsRes(totalAmount, transactionCount);
    }


    // 获取用户的所有角色ID
    private Set<Long> getUserRoleIds(UserPrinciple user) {
        Set<Long> roleIds = new HashSet<>();

        for (GrantedAuthority authority : user.getAuthorities()) {
            String roleName = authority.getAuthority();
            if (roleName.equals("ROLE_ADMIN")) {
                roleIds.add(1L);
            } else if (roleName.equals("ROLE_MANUFACTURER")) { // 加盟商
                roleIds.add(2L);
            } else if (roleName.equals("ROLE_STORE_MANAGER")) { // 店家店长
                roleIds.add(5L);
            }
        }

        return roleIds;
    }

    // 获取最高优先级的角色
// 1 > 2 > 5, 即管理员 > 加盟商 > 店家
    private Long getHighestPriorityRole(Set<Long> roleIds) {
        if (roleIds.contains(1L)) {
            return 1L; // 管理员
        } else if (roleIds.contains(2L)) {
            return 2L; // 加盟商
        } else if (roleIds.contains(5L)) {
            return 5L; // 店家
        }
        return 5L; // 默认为店家角色
    }


    // 獲取儲值總額（不按店家和廠商過濾）
    private TransactionsRes getTotalDeposits() {
        List<TransactionsRes> totalDeposits = transactionRecordRepository.getTotalDeposits();
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer transactionCount = 0;

        for (TransactionsRes res : totalDeposits) {
            totalAmount = totalAmount.add(res.getTodayTotalAmount());
            transactionCount += res.getTodayTransactionCount();
        }

        return new TransactionsRes(totalAmount, transactionCount);
    }

    // 獲取消費總額（按店家和廠商過濾）
    private TransactionsRes getTotalConsumptionFiltered(Long highestRoleId, Long vendorId, List<Long> storeIds) {
        List<TransactionsRes> totalConsumption = gameOrderRepository.getTotalConsumptionFiltered(highestRoleId, vendorId, storeIds);
        BigDecimal totalAmount = BigDecimal.ZERO;
        Integer transactionCount = 0;

        for (TransactionsRes res : totalConsumption) {
            totalAmount = totalAmount.add(res.getTodayTotalAmount());
            transactionCount += res.getTodayTransactionCount();
        }

        return new TransactionsRes(totalAmount, transactionCount);
    }



}
