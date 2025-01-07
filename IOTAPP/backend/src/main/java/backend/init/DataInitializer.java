package backend.init;

import backend.entity.menu.Menu;
import backend.entity.role.Role;
import backend.enums.RoleName;
import backend.repo.MenuRepository;
import backend.repo.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private MenuRepository menuRepository;

    @Override
    public void run(String... args) throws Exception {
        // 取得菜單資料
        Menu menu1 = menuRepository.findById(1L).orElse(null);
        Menu menu2 = menuRepository.findById(2L).orElse(null);
        Menu menu3 = menuRepository.findById(3L).orElse(null);
        Menu menu4 = menuRepository.findById(4L).orElse(null);
        Menu menu5 = menuRepository.findById(5L).orElse(null);
        Menu menu6 = menuRepository.findById(6L).orElse(null);

        // 取得角色資料
        Optional<Role> roleAdmin = roleRepository.findByRoleName(RoleName.ROLE_ADMIN);
        Optional<Role> roleVendor = roleRepository.findByRoleName(RoleName.ROLE_MANUFACTURER);
        Optional<Role> roleMember = roleRepository.findByRoleName(RoleName.ROLE_USER);
        Optional<Role> roleBlacklist = roleRepository.findByRoleName(RoleName.ROLE_BLACKLIST);

        // 初始化 ROLE_ADMIN 的菜單關聯
        roleAdmin.ifPresent(role -> {
            if (menu1 != null) {
                role.getMenus().add(menu1);
                role.getMenus().add(menu2);
                role.getMenus().add(menu3);
                role.getMenus().add(menu4);
                role.getMenus().add(menu5);
                role.getMenus().add(menu6);
                roleRepository.save(role);  // 更新角色資料
            }
        });

        // 初始化 ROLE_VENDOR 的菜單關聯
        roleVendor.ifPresent(role -> {
            if (menu1 != null) {
                role.getMenus().add(menu1);
                role.getMenus().add(menu2);
                role.getMenus().add(menu3);
                roleRepository.save(role);  // 更新角色資料
            }
        });

        // 初始化 ROLE_MEMBER 的菜單關聯
        roleMember.ifPresent(role -> {
            if (menu1 != null) {
                role.getMenus().add(menu1);
                role.getMenus().add(menu2);
                roleRepository.save(role);  // 更新角色資料
            }
        });

        // 初始化 ROLE_BLACKLIST 的菜單關聯
        roleBlacklist.ifPresent(role -> {
            if (menu1 != null) {
                role.getMenus().add(menu1);
                roleRepository.save(role);  // 更新角色資料
            }
        });
    }
}
