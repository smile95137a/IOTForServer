package backend.service;

import backend.entity.menu.Menu;
import backend.entity.role.Role;
import backend.repo.MenuRepository;
import backend.repo.RoleRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MenuRepository menuRepository;

    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    public Role findRoleById(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with id " + roleId));
    }

    public Role createRole(Role role) {
        return roleRepository.save(role);
    }

    public Role updateRole(Long roleId, Role updatedRole) {
        Role existingRole = findRoleById(roleId);
        existingRole.setRoleName(updatedRole.getRoleName());
        existingRole.setDescription(updatedRole.getDescription());
        return roleRepository.save(existingRole);
    }

    public void deleteRole(Long roleId) {
        roleRepository.deleteById(roleId);
    }

    public Role assignMenusToRole(Long roleId, List<Long> menuIds) {
        Role role = findRoleById(roleId);
        List<Menu> allById = menuRepository.findAllById(menuIds);
        Set<Menu> menus = new HashSet<Menu>(allById);
        role.setMenus(menus);
        return roleRepository.save(role);
    }

    @Transactional
    @Query("SELECT r FROM Role r LEFT JOIN FETCH r.menus WHERE r.id = :roleId")
    public Set<Menu> findMenusByRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return role.getMenus();
    }
}
