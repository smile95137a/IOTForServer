package com.frontend.repo;

import com.frontend.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
	Optional<User> findByUsername(String username);

	Optional<User> findByEmail(String email);

	Optional<User> findByPhoneNumber(String phoneNumber);
	
	Optional<User> findByCountryCodeAndPhoneNumber(String countryCode, String phoneNumber);

	User findByUid(String userUid);
}