package com.frontend.service;

import com.frontend.entity.menu.Menu;
import com.frontend.entity.role.Role;
import com.frontend.entity.user.User;
import com.frontend.repo.MenuRepository;
import com.frontend.repo.RoleRepository;
import com.frontend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 查找所有菜单
     */
    public List<Menu> findAllMenus(Long userId) {
        // 获取用户信息
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        // 获取用户的角色集合
        Set<Role> roles = user.getRoles();

        // 使用 Set 来去重
        Set<Menu> uniqueMenus = new HashSet<>();

        // 遍历角色，获取对应的菜单
        for (Role role : roles) {
            // 根据角色 ID 查找菜单
            List<Menu> menus = menuRepository.findByRolesId(role.getId());
            uniqueMenus.addAll(menus); // 添加到 Set，自动去重
        }

        // 返回去重后的菜单列表
        return new ArrayList<>(uniqueMenus);
    }


    /**
     * 根据ID查找菜单
     */
    public Menu findMenuById(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new RuntimeException("Menu not found with id " + menuId));
    }

    /**
     * 创建菜单
     */
    public List<Menu> createMenus(List<Menu> menus) {
        for (Menu menu : menus) {
            // 如果有父菜单，检查其是否存在
            if (menu.getParent() != null && menu.getParent().getId() != null) {
                Menu parentMenu = menuRepository.findById(menu.getParent().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Parent menu not found with ID: " + menu.getParent().getId()));
                menu.setParent(parentMenu);
            }
        }
        // 批量保存所有菜单
        return menuRepository.saveAll(menus);
    }


    /**
     * 更新菜单
     */
    public Menu updateMenu(Long menuId, Menu updatedMenu) {
        Menu existingMenu = findMenuById(menuId);

        // 更新基本字段
        existingMenu.setMenuName(updatedMenu.getMenuName());
        existingMenu.setUrl(updatedMenu.getUrl());
        existingMenu.setIcon(updatedMenu.getIcon());
        existingMenu.setMenuOrder(updatedMenu.getMenuOrder());
        existingMenu.setStatus(updatedMenu.getStatus());

        // 处理父菜单更新逻辑
        if (updatedMenu.getParent() != null) {
            Menu newParent = findMenuById(updatedMenu.getParent().getId());

            // 防止循环依赖
            if (newParent.getId().equals(menuId)) {
                throw new IllegalArgumentException("Menu cannot be its own parent");
            }
            existingMenu.setParent(newParent);
        } else {
            existingMenu.setParent(null); // 清空父菜单
        }

        return menuRepository.save(existingMenu);
    }

    /**
     * 删除菜单
     */
    public boolean deleteMenu(Long menuId) {
        Menu menuToDelete = findMenuById(menuId);

        // 清理子菜单关系
        for (Menu child : menuToDelete.getChildren()) {
            child.setParent(null);
            menuRepository.save(child);
        }

        // 删除菜单
        menuRepository.delete(menuToDelete);
        return true;
    }
}
