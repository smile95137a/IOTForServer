package src.main.java.com.frontend.mapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import src.main.java.com.frontend.entity.pwd.PasswordResetToken;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class PasswordResetTokenMapper {

	public PasswordResetToken toPasswordResetToken(Long userId, String token, int expiryMinutes) {
		var now = LocalDateTime.now();
		var expiresAt = now.plus(expiryMinutes, ChronoUnit.MINUTES);

		return PasswordResetToken.builder()
				.userId(userId)
				.token(token)
				.isActive(false)
				.passwordChanged(false)
				.createTime(now)
				.expireTime(expiresAt)
				.build();
	}
}
