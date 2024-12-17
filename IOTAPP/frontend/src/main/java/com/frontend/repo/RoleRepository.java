package com.frontend.repo;

import java.util.Optional;

import backend.entity.user.Role;
import backend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(RoleName roleName);

	boolean existsByName(RoleName roleName);
}