package com.frontend.repo;

import com.frontend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhoneNumber(String phoneNumber);
	
	Optional<User> findByCountryCodeAndPhoneNumber(String countryCode, String phoneNumber);

	User findByUid(String userUid);
	
	

	Optional<User> findByuid(String uid);


	Boolean existsByUsername(String username);

	Boolean existsByEmail(String email);

	void deleteByuid(String uid);

	@Query("SELECT u.id, u.name, u.amount FROM User u")
	List<Object[]> getAllUserRemainingBalance();  // 获取所有用户的剩余储值金额

}