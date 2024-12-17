package com.frontend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import backend.config.service.UserPrinciple;
import backend.entity.user.Role;
import backend.entity.user.User;
import backend.enums.RoleName;
import backend.mapper.UserMapper;
import backend.repo.RoleRepository;
import backend.repo.UserRepository;
import backend.req.user.UserReq;
import backend.res.user.UserRes;
import backend.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;

	private final RoleRepository roleRepository;

	private final PasswordEncoder passwordEncoder;

	private final MailService mailService;

	private final UserMapper userMapper;
	
	public List<User> getAllUsers() {
		return userRepository.findAll();
	}

	public User getUserById(Long id) {
		var optional = userRepository.findById(id);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}

	public User getUserByuid(String uid) {
		var optional = userRepository.findByuid(uid);
		if (optional.isPresent()) {
			return optional.get();
		}
		return null;
	}
	@Transactional(rollbackFor = { Exception.class })
	public UserRes createUser(UserReq userReq) throws Exception {

	    if (userRepository.existsByUsername(userReq.getUsername())) {
	        throw new Exception("使用者名稱已被使用");
	    }

	    if (userRepository.existsByEmail(userReq.getEmail())) {
	        throw new Exception("電子郵件已被使用");
	    }

	    Set<Role> roles = new HashSet<>();
	    Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow(() -> new Exception("找不到使用者角色"));
	    roles.add(userRole);

	    var userEntity = userMapper.mapToUser(userReq);
	    userEntity.setRoles(roles);
	    userRepository.save(userEntity);

	    var res = userMapper.mapToUserRes(userEntity);

	    var isSendEmail = userReq.getIsSendEmail();
	    if (isSendEmail) {
	        mailService.sendPasswordEmail(userReq.getEmail(), userReq.getName(), userReq.getPassword());
	    }

	    return res;
	}


	@Transactional(rollbackFor = { Exception.class })
	public UserRes updateUser(UserReq userReq) {
		var entity = this.getUserByuid(userReq.getUid());
		if (entity != null) {
			var principal = (UserPrinciple) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			var reqPwd = userReq.getPassword();
			var ePwd = entity.getPassword();
			var pwd = StringUtils.equals(reqPwd, ePwd) ? ePwd : passwordEncoder.encode(reqPwd);

			entity.setName(userReq.getName());
			entity.setPassword(pwd);
			entity.setEmail(userReq.getEmail());
			entity.setUpdateTime(LocalDateTime.now());
			entity.setUpdateUserId(principal.getId());

			entity = userRepository.save(entity);
			var res = UserRes.builder().build();
			BeanUtils.copyProperties(entity, res);
			return res;
		}
		return null;
	}

	@Transactional(rollbackFor = { Exception.class })
	public boolean deleteUser(String id) {
		var entity = this.getUserByuid(id);
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
}