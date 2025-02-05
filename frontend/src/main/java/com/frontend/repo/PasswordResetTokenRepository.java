package src.main.java.com.frontend.repo;

import java.util.List;
import java.util.Optional;

import src.main.java.com.frontend.entity.pwd.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	Optional<PasswordResetToken> findByToken(String token);

	List<PasswordResetToken> findByUserIdOrderByCreateTimeDesc(Long userId);
}