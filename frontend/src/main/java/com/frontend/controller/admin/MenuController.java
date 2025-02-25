package com.frontend.controller.admin;

import com.frontend.config.message.ApiResponse;
import com.frontend.config.service.UserPrinciple;
import com.frontend.entity.menu.Menu;
import com.frontend.service.MenuService;
import com.frontend.utils.ResponseUtils;
import com.frontend.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/b/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    // 1. 查询所有菜单（支持树形结构）
    @GetMapping
    public ResponseEntity<ApiResponse<List<Menu>>> getAllMenus() {
        UserPrinciple securityUser = SecurityUtils.getSecurityUser();
        Long id = securityUser.getId();
        List<Menu> allMenus = menuService.findAllMenus(id);
        if (allMenus.isEmpty()) {
            ApiResponse<List<Menu>> error = ResponseUtils.error(List.of());
            return ResponseEntity.ok(error);
        }
        ApiResponse<List<Menu>> success = ResponseUtils.success(allMenus);
        return ResponseEntity.ok(success);
    }

    // 2. 查询单个菜单详情
    // 根据菜单 ID 获取菜单
    @GetMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Menu>> getMenuById(@PathVariable Long menuId) {
        Menu menu = menuService.findMenuById(menuId);
        if (menu == null) {
            ApiResponse<Menu> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<Menu> success = ResponseUtils.success(menu);
        return ResponseEntity.ok(success);
    }

    // 新增菜单
    @PostMapping
    public ResponseEntity<ApiResponse<List<Menu>>> createMenus(@RequestBody List<Menu> menus) {
        List<Menu> createdMenus = menuService.createMenus(menus);
        if (createdMenus.isEmpty()) {
            ApiResponse<List<Menu>> error = ResponseUtils.error(List.of());
            return ResponseEntity.ok(error);
        }
        ApiResponse<List<Menu>> success = ResponseUtils.success(createdMenus);
        return ResponseEntity.ok(success);
    }

    // 更新菜单
    @PutMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Menu>> updateMenu(@PathVariable Long menuId, @RequestBody Menu menu) {
        Menu updatedMenu = menuService.updateMenu(menuId, menu);
        if (updatedMenu == null) {
            ApiResponse<Menu> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<Menu> success = ResponseUtils.success(updatedMenu);
        return ResponseEntity.ok(success);
    }

    // 删除菜单
    @DeleteMapping("/{menuId}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable Long menuId) {
        boolean isDeleted = menuService.deleteMenu(menuId);
        if (!isDeleted) {
            ApiResponse<Void> error = ResponseUtils.error(null);
            return ResponseEntity.ok(error);
        }
        ApiResponse<Void> success = ResponseUtils.success(null);
        return ResponseEntity.ok(success);
    }

}
