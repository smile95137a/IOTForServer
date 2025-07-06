package com.frontend.service;

import com.frontend.controller.admin.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.entity.verification.VerificationToken;
import com.frontend.enums.RoleName;
import com.frontend.repo.RoleRepository;
import com.frontend.repo.UserRepository;
import com.frontend.repo.VerificationTokenRepository;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.utils.RandomUtils;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private VerificationTokenRepository tokenRepository;

	@Autowired
	private MailService mailService;

	@Value("${verification.url}")
	private String verificationUrl;

	@Autowired
	private UserMapper userMapper;

	public UserRes getUserById(Long userId) {
		User user = userRepository.findById(userId).get();

		return userMapper.mapToUserRes(user);
	}

	public UserRes registerUser(UserReq userDto) throws Exception {
		try {
			// 1. 检查用户是否存在
			Optional<User> check = userRepository.findByEmail(userDto.getEmail());
			if (check.isPresent()) {
				throw new Exception("帳號已存在");
			}

			Optional<User> checkPhone = userRepository.findByPhoneNumber(userDto.getPhone());
			if (checkPhone.isPresent()) {
				throw new Exception("電話已存在");
			}


			// 2. 获取角色和加密密码
			Optional<Role> memberRole = roleRepository.findByRoleName(RoleName.ROLE_USER);
			String encryptedPassword = passwordEncoder.encode(userDto.getPassword());

			// 3. 创建用户并保存
			// 1. 创建 User
			User user = User.builder()
					.uid(RandomUtils.genRandom(6))
					.countryCode(userDto.getCountryCode())
					.gender(userDto.getGender())
					.verificationCode(userDto.getVerificationCode())
					.anonymousId(userDto.getAnonymousId())
					.password(encryptedPassword)
					.email(userDto.getEmail())
					.phoneNumber(userDto.getPhone())
					.createTime(LocalDateTime.now())
					.name(userDto.getName())
					.amount(0)
					.balance(0)
					.point(0)
					.totalAmount(0)
					.nickName(userDto.getNickName())
					.build();

// 2. 查找 `ROLE_USER` 角色
			Role userRole = roleRepository.findByRoleName(RoleName.ROLE_USER)
					.orElseThrow(() -> new RuntimeException("Default role ROLE_USER not found"));

// 3. 绑定角色
			user.setRoles(Set.of(userRole));

// 4. 保存用户
			userRepository.save(user);

			// 5. 生成验证链接并发送邮件
			generateVerificationTokenAndSendEmail(user);

			// 6. 返回用户信息
			UserRes userRes = new UserRes();
			userRes.setUsername(user.getUsername());
			userRes.setEmail(user.getEmail());
			userRes.setUid(user.getUid());
			userRes.setId(user.getId());
			userRes.setRoles(user.getRoles());
			userRes.setNickName(user.getNickName());
			return userRes;

		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}


	@Transactional
	public UserRes updateUser(UserReq req, Long userId) throws Exception {
		try {
			Optional<User> userObj = userRepository.findById(userId);
			User user = userObj.get();
			user.setName(req.getName());
			user.setEmail(req.getEmail());
			user.setUpdateTime(LocalDateTime.now());
			user.setAnonymousId(req.getAnonymousId());
			user.setNickName(req.getNickName());
			userRepository.save(user);
			UserRes userRes = new UserRes();
			userRes.setUsername(user.getUsername());
			userRes.setEmail(user.getEmail());
			userRes.setUid(user.getUid());
			userRes.setId(user.getId());
			userRes.setNickName(user.getNickName());
			userRes.setAnonymousId(user.getAnonymousId());
			return userRes;
		} catch (Exception e) {
			throw new Exception("Failed to update user with ID: " + userId, e);
		}
	}

	public void generateVerificationTokenAndSendEmail(User user) {
		// 1. 生成唯一 token
		String token = UUID.randomUUID().toString();
		VerificationToken verificationToken = new VerificationToken();
		verificationToken.setToken(token);
		verificationToken.setUserId(user.getId());
		verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24)); // 设置 24 小时过期
		tokenRepository.save(verificationToken);

		// 2. 生成验证链接
		String verificationUrls = verificationUrl + token;

		// 3. 发送邮件
		mailService.sendVerificationMail(user.getEmail(), verificationUrls);
	}

	public static UserRes convertToUserRes(User user) {
			if (user == null) {
				return null;
			}

		return UserRes.builder()
				.id(user.getId())
				.anonymousId(user.getAnonymousId())
				.uid(user.getUid())
				.username(user.getUsername())
				.password(user.getPassword())
				.gender(user.getGender())
				.name(user.getName())
				.email(user.getEmail())
				.roles(new HashSet<>(user.getRoles())) // Collect to Set<Role>
				.createTime(user.getCreateTime())
				.createUserName(user.getCreateUserId() != null ? user.getCreateUserId().toString() : null)
				.updateTime(user.getUpdateTime())
				.updateUserName(user.getUpdateUserId() != null ? user.getUpdateUserId().toString() : null)
				.lastActiveTime(user.getLastActiveTime())
				.amount(user.getAmount())
				.totalAmount(user.getTotalAmount())
				.imgUrl(user.getUserImg())
				.nickName(user.getNickName())
				.point(user.getPoint())
				.totalAmount(user.getTotalAmount())
				.balance(user.getBalance())
				.build();

	}

	public void uploadProductImg(Long userId, String uploadedFilePaths) {
		User user = userRepository.findById(userId).get();
		if (user != null) {
			user.setUserImg(uploadedFilePaths);
			userRepository.save(user);
		}

	}
}