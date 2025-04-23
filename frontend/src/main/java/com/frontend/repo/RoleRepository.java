package com.frontend.repo;


import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByRoleName(RoleName roleName);

	Set<Role> findByRoleNameIn(Set<RoleName> roleNames);

	Set<Role> findByIdIn(Set<Long> roleIds);

	@Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
	Set<User> findUsersByRoleId(@Param("roleId") Long roleId);
}