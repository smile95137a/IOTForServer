package backend.controller;

import backend.entity.menu.Menu;
import backend.service.MenuService;
import backend.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import backend.entity.role.Role;
import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;

    // 1. 查询所有角色
    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.findAllRoles();
    }

    // 2. 查询单个角色详情 (包含菜单)
    @GetMapping("/{roleId}")
    public Role getRoleById(@PathVariable Long roleId) {
        return roleService.findRoleById(roleId);
    }

    // 3. 创建新角色
    @PostMapping
    public Role createRole(@RequestBody Role role) {
        return roleService.createRole(role);
    }

    // 4. 更新角色信息
    @PutMapping("/{roleId}")
    public Role updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        return roleService.updateRole(roleId, role);
    }

    // 5. 删除角色
    @DeleteMapping("/{roleId}")
    public void deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
    }

    // 6. 设置角色的菜单权限
    @PostMapping("/{roleId}/menus")
    public Role assignMenusToRole(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        return roleService.assignMenusToRole(roleId, menuIds);
    }

    // 7. 查询角色的菜单权限
    @GetMapping("/{roleId}/menus")
    public List<Menu> getMenusByRole(@PathVariable Long roleId) {
        return roleService.findMenusByRole(roleId);
    }
}
