package src.main.java.backend.repo;


import src.main.java.backend.entity.role.Role;
import src.main.java.backend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByRoleName(RoleName roleName);

}