package backend.controller;

import backend.config.message.ApiResponse;
import backend.entity.menu.Menu;
import backend.entity.role.Role;
import backend.req.MenuIdsRequest;
import backend.service.MenuService;
import backend.service.RoleService;
import backend.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private MenuService menuService;

    // 1. 查询所有角色
    @GetMapping
    public ResponseEntity<ApiResponse<List<Role>>> getAllRoles() {
        List<Role> roles = roleService.findAllRoles();
        ApiResponse<List<Role>> success = ResponseUtils.success(roles);
        return ResponseEntity.ok(success);
    }

    // 2. 查询单个角色详情 (包含菜单)
    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Role>> getRoleById(@PathVariable Long roleId) {
        Role role = roleService.findRoleById(roleId);
        if (role == null) {
            ApiResponse<Role> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<Role> success = ResponseUtils.success(role);
        return ResponseEntity.ok(success);
    }

    // 3. 创建新角色
    @PostMapping
    public ResponseEntity<ApiResponse<Role>> createRole(@RequestBody Role role) {
        Role createdRole = roleService.createRole(role);
        ApiResponse<Role> success = ResponseUtils.success(createdRole);
        return ResponseEntity.ok(success);
    }

    // 4. 更新角色信息
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Role>> updateRole(@PathVariable Long roleId, @RequestBody Role role) {
        try {
            Role updatedRole = roleService.updateRole(roleId, role);
            ApiResponse<Role> success = ResponseUtils.success(updatedRole);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Role> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // 5. 删除角色
    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long roleId) {
        try {
            roleService.deleteRole(roleId);
            ApiResponse<Void> success = ResponseUtils.success(null);
            return ResponseEntity.ok(success);
        } catch (RuntimeException e) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
    }

    // 6. 设置角色的菜单权限
    @PostMapping("/{roleId}/menus")
    public ResponseEntity<ApiResponse<Role>> assignMenusToRole(@PathVariable Long roleId, @RequestBody MenuIdsRequest menuIdsRequest) {
        System.out.println(menuIdsRequest.getMenuIds());
        Role updatedRole = roleService.assignMenusToRole(roleId, menuIdsRequest.getMenuIds());
        ApiResponse<Role> success = ResponseUtils.success(updatedRole);
        return ResponseEntity.ok(success);
    }

    // 7. 查询角色的菜单权限
    @GetMapping("/{roleId}/menus")
    public ResponseEntity<ApiResponse<Set<Menu>>> getMenusByRole(@PathVariable Long roleId) {
        Set<Menu> menus = roleService.findMenusByRole(roleId);
        ApiResponse<Set<Menu>> success = ResponseUtils.success(menus);
        return ResponseEntity.ok(success);
    }
}
