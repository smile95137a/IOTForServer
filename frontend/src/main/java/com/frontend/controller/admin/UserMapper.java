package com.frontend.controller.admin;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.frontend.entity.transection.UserTransactionsRes;
import com.frontend.repo.GameOrderRepository;
import com.frontend.repo.TransactionRecordRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.frontend.entity.user.User;
import com.frontend.repo.UserRepository;
import com.frontend.req.user.UserReq;
import com.frontend.res.user.UserRes;
import com.frontend.utils.RandomUtils;
import com.frontend.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	private final GameOrderRepository gameOrderRepository;

	private final TransactionRecordRepository transactionRecordRepository;


	public User mapToUser(UserReq userReq) {

		var userId = SecurityUtils.getSecurityUser().getId();

		return User.builder().username(userReq.getUsername()).password(passwordEncoder.encode(userReq.getPassword()))
				.name(userReq.getName()).email(userReq.getEmail()).uid(RandomUtils.genRandom(32)).createUserId(userId)
				.createTime(LocalDateTime.now()).build();
	}

	public UserRes mapToUserRes(User userEntity) {
	    String createName = null;
	    String updateName = null;

	    // Check if createUserId is not null before querying
	    if (userEntity.getCreateUserId() != null) {
	        createName = userRepository.findById(userEntity.getCreateUserId())
	                                   .map(User::getName)
	                                   .orElse("Unknown"); // Provide a default value if not found
	    }

	    // Check if updateUserId is not null before querying
	    if (userEntity.getUpdateUserId() != null) {
	        updateName = userRepository.findById(userEntity.getUpdateUserId())
	                                   .map(User::getName)
	                                   .orElse("Unknown"); // Provide a default value if not found
	    }

		// 获取某个用户的消费总金额与消费笔数
		UserTransactionsRes consumptionData = gameOrderRepository.getTotalConsumptionAmountAndCount(userEntity.getUid());

// 获取某个用户的充值总金额与充值笔数
		UserTransactionsRes depositData = transactionRecordRepository.getTotalDepositsAmountAndCount(userEntity.getId());


		return UserRes.builder()
	                  .id(userEntity.getId())
	                  .uid(userEntity.getUid())
	                  .username(userEntity.getUsername())
	                  .password(userEntity.getPassword())
	                  .name(userEntity.getName())
	                  .email(userEntity.getEmail())
	                  .roles(userEntity.getRoles())
	                  .createTime(userEntity.getCreateTime())
	                  .updateTime(userEntity.getUpdateTime())
	                  .lastActiveTime(userEntity.getLastActiveTime())
	                  .createUserName(createName)
	                  .updateUserName(updateName)
				.anonymousId(userEntity.getAnonymousId())
				.balance(userEntity.getBalance())
				.point(userEntity.getPoint())
				.nickName(userEntity.getNickName())
				.gender(userEntity.getGender())
				.imgUrl(userEntity.getUserImg())
				.amount(userEntity.getAmount())
				.userImg(userEntity.getUserImg())
				// 填充总消费金额和笔数
				.totalConsumptionAmount(consumptionData.getTotalAmount())
				.totalConsumptionCount(consumptionData.getCount())

				// 填充总储值金额和笔数
				.totalDepositsAmount(depositData.getTotalAmount())
				.totalDepositsCount(depositData.getCount())
	                  .build();
	}



}
