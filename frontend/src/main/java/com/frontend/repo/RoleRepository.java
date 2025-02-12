package com.frontend.repo;

import java.util.Optional;
import java.util.Set;

import com.frontend.entity.role.Role;
import com.frontend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByRoleName(RoleName roleName);

	Set<Role> findByRoleNameIn(Set<RoleName> roleNames);
}