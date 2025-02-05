package src.main.java.com.frontend.repo;

import java.util.Optional;

import src.main.java.com.frontend.entity.role.Role;
import src.main.java.com.frontend.enums.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Optional<Role> findByName(RoleName roleName);
}