package src.main.java.backend.service;

import src.main.java.backend.entity.menu.Menu;
import src.main.java.backend.repo.MenuRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    /**
     * 查找所有菜单
     */
    public List<Menu> findAllMenus() {
        return menuRepository.findAll();
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
