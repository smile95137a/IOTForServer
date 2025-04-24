package com.frontend.controller.admin;


import com.frontend.config.message.ApiResponse;
import com.frontend.config.security.SecurityUtils;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.transection.TransactionRecord;
import com.frontend.entity.user.User;
import com.frontend.repo.GameOrderRepository;
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

@RestController
@RequestMapping("/api/b/turnover")
public class turnoverController {

    @Autowired
    private TransactionRecordRepository transactionRecordRepository;


    @Autowired
    private GameOrderRepository gameOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<TransactionsRes>> getTransactionsSummary() {
        // 获取当前登录用户
        UserPrinciple currentUser = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 获取用户的所有角色ID
        Set<Long> roleIds = getUserRoleIds(currentUser);

        Long vendorId = null;
        Long storeId = null;
        Long highestRoleId = getHighestPriorityRole(roleIds);

        // 根据角色获取关联ID
        if (roleIds.contains(2L) || highestRoleId == 2L) { // 加盟商（Vendor）
            vendorId = user.getVendor() != null ? user.getVendor().getId() : null;
        } else if (roleIds.contains(5L) || highestRoleId == 5L) { // 店家店长（Store）
            storeId = user.getStore() != null ? user.getStore().getId() : null;
        }

        // 获取今天的储值总金额和笔数 (根据角色过滤)
        TransactionsRes todayDeposits = transactionRecordRepository.getTodayTotalDeposits(highestRoleId, vendorId, storeId);

        // 获取今天的消费总金额和笔数 (根据角色过滤)
        TransactionsRes todayConsumption = gameOrderRepository.getTodayTotalConsumption(highestRoleId, vendorId, storeId);

        // 获取本月的总消费金额和笔数 (根据角色过滤)
        TransactionsRes monthConsumption = gameOrderRepository.getMonthTotalConsumption(highestRoleId, vendorId, storeId);

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

        // 返回响应
        return ResponseEntity.ok(ResponseUtils.success(200, null, summaryRes));
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
            } else if (roleName.equals("ROLE_USER")) { // 店家店长
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





}
