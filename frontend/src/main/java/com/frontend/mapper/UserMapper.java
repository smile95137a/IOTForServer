package src.main.java.com.frontend.mapper;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

import src.main.java.com.frontend.entity.user.User;
import src.main.java.com.frontend.repo.UserRepository;
import src.main.java.com.frontend.req.user.UserReq;
import src.main.java.com.frontend.res.user.UserRes;
import src.main.java.com.frontend.utils.RandomUtils;
import src.main.java.com.frontend.utils.SecurityUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserMapper {

	private final PasswordEncoder passwordEncoder;

	private final UserRepository userRepository;

	public User mapToUser(UserReq userReq) {

		var userId = SecurityUtils.getSecurityUser().getId();

		return User.builder().username(userReq.getUsername()).password(passwordEncoder.encode(userReq.getPassword()))
				.name(userReq.getName()).email(userReq.getEmail()).uid(RandomUtils.genRandom(24)).createUserId(userId)
				.createTime(LocalDateTime.now()).money(0L).couponList(new ArrayList<>()).build();
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

	    return UserRes.builder()
	                  .id(userEntity.getId())
	                  .uid(userEntity.getUid())
	                  .username(userEntity.getUsername())
	                  .password(userEntity.getPassword())
	                  .name(userEntity.getName())
	                  .email(userEntity.getEmail())
	                  .roles(userEntity.getRoles().stream()
	                                    .map(role -> role.getName().name())
	                                    .collect(Collectors.toSet()))
	                  .createTime(userEntity.getCreateTime())
	                  .updateTime(userEntity.getUpdateTime())
	                  .lastActiveTime(userEntity.getLastActiveTime())
	                  .createUserName(createName)
	                  .updateUserName(updateName)
	                  .build();
	}

}
