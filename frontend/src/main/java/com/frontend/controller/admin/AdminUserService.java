package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.enums.RoleName;
import com.frontend.repo.RoleRepository;
import com.frontend.repo.UserRepository;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.service.MailService;
import com.frontend.utils.SecurityUtils;

import io.micrometer.common.util.StringUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;

    private final UserMapper userMapper;

    public List<UserRes> getAllUsers() {
        List<UserRes> userResList = new ArrayList<>();
        for (User user : userRepository.findAll()) {
            UserRes userRes = userMapper.mapToUserRes(user);
            userResList.add(userRes);
        }
        return  userResList;
    }

    public UserRes getByUid(String uid) {
        User user = userRepository.findByuid(uid).get();
        return userMapper.mapToUserRes(user);
    }

    @Transactional(rollbackFor = {Exception.class})
    public UserRes createUser(UserReq userReq) throws Exception {

        if (userRepository.existsByUsername(userReq.getUsername())) {
            throw new Exception("使用者名稱已被使用");
        }

        if (userRepository.existsByEmail(userReq.getEmail())) {
            throw new Exception("電子郵件已被使用");
        }

        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER).orElseThrow(() -> new Exception("找不到使用者角色"));
        roles.add(userRole);

        var userEntity = userMapper.mapToUser(userReq);
        userEntity.setRoles(roles);
        userRepository.save(userEntity);

        var res = userMapper.mapToUserRes(userEntity);
        mailService.sendPasswordEmail(userReq.getEmail(), userReq.getName(), userReq.getPassword());
        return res;
    }


    @Transactional(rollbackFor = {Exception.class})
    public UserRes updateUser(UserReq userReq , Long id) {
        var entity = userRepository.findById(id).get();
        if (entity != null) {
            var principal = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            var ePwd = entity.getPassword();
            Set<Role> byIdIn = roleRepository.findByRoleNameIn(userReq.getRoleNames());
            entity.setName(userReq.getName());
            entity.setPassword(ePwd);
            entity.setEmail(userReq.getEmail());
            entity.setUpdateTime(LocalDateTime.now());
            entity.setUpdateUserId(principal.getId());
            entity.setPhoneNumber(userReq.getPhone());
            entity.setRoles(byIdIn);
            entity.setAnonymousId(userReq.getAnonymousId());
            entity.setNickName(userReq.getNickName());
            userRepository.save(entity);
            var res = UserRes.builder().build();
            BeanUtils.copyProperties(entity, res);
            return res;
        }
        return null;
    }

    @Transactional(rollbackFor = {Exception.class})
    public boolean deleteUser(Long id) {
        var entity = userRepository.findById(id).orElse(null);
        if (entity != null) {
            userRepository.delete(entity);
            return true;
        }
        return false;
    }

    public List<UserRes> queryUser(UserReq req) {
        List<User> userList = userRepository.findAll((root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(req.getUsername())) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + req.getUsername() + "%"));
            }

            if (StringUtils.isNotBlank(req.getEmail())) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + req.getEmail() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });

        var detail = SecurityUtils.getSecurityUser();
        var authorities = detail.getAuthorities();
        var uid = detail.getUid();
        boolean isAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(role -> role.equals("ROLE_ADMIN"));

        if (!isAdmin) {
            userList = userList.stream()
                    .filter(user -> user.getUid().equals(uid))
                    .collect(Collectors.toList());
        }

        return userList.stream()
                .map(userMapper::mapToUserRes)
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateBlack(List<Long> userIds) {
        Role blacklistRole = roleRepository.findByRoleName(RoleName.ROLE_BLACKLIST)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<User> users = userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            throw new RuntimeException("No users found for the given IDs");
        }

        for (User user : users) {
            user.getRoles().add(blacklistRole);
        }

        userRepository.saveAll(users);
    }


    @Transactional
    public void removeBlackList(List<Long> userIds) {
        // 取得黑名單角色
        Role blacklistRole = roleRepository.findByRoleName(RoleName.ROLE_BLACKLIST)
                .orElseThrow(() -> new RuntimeException("Blacklist role not found"));
        List<User> users = userRepository.findAllById(userIds);

        if (users.isEmpty()) {
            throw new RuntimeException("No users found for the given IDs");
        }

        for (User user : users) {
            user.getRoles().remove(blacklistRole);
        }

        userRepository.saveAll(users);
    }

}