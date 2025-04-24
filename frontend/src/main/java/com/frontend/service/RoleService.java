package com.frontend.service;

import com.frontend.entity.menu.Menu;
import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.repo.MenuRepository;
import com.frontend.repo.RoleRepository;
import com.frontend.res.user.UserRoleRes;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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

    public List<UserRoleRes> findUsersByRole(Long roleId) {
        Set<User> usersByRoleId = roleRepository.findUsersByRoleId(roleId);
        List<UserRoleRes> list = new ArrayList<>();

        usersByRoleId.forEach(user -> {
            // 條件過濾
            if ((roleId == 2 && user.getVendor() != null) || (roleId == 5 && user.getStore() != null)) {
                return; // 符合條件就跳過
            }

            UserRoleRes roleRes = new UserRoleRes();
            roleRes.setId(user.getId());
            roleRes.setUid(user.getUid());
            roleRes.setName(user.getName());
            list.add(roleRes);
        });

        return list;
    }
}
