package backend.controller;

import backend.entity.menu.Menu;
import backend.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // 1. 查询所有菜单（支持树形结构）
    @GetMapping
    public List<Menu> getAllMenus() {
        return menuService.findAllMenus();
    }

    // 2. 查询单个菜单详情
    @GetMapping("/{menuId}")
    public Menu getMenuById(@PathVariable Long menuId) {
        return menuService.findMenuById(menuId);
    }

    // 3. 新增菜单
    @PostMapping
    public List<Menu> createMenus(@RequestBody List<Menu> menus) {
        return menuService.createMenus(menus);
    }

    // 4. 更新菜单
    @PutMapping("/{menuId}")
    public Menu updateMenu(@PathVariable Long menuId, @RequestBody Menu menu) {
        return menuService.updateMenu(menuId, menu);
    }

    // 5. 删除菜单
    @DeleteMapping("/{menuId}")
    public void deleteMenu(@PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
    }
}
